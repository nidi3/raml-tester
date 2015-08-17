/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;

import java.util.*;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.core.CheckerHelper.paramEntries;

/**
 *
 */
public class RamlValidator {
    private enum ParamName {
        BASE_URI("baseUriParameter"),
        URI("uriParameter"),
        QUERY("queryParameter"),
        HEADER("header"),
        FORM("formParameter");
        private final String value;

        ParamName(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Validation {
        URI_PARAMETER, EXAMPLE, DESCRIPTION
    }

    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final EnumSet<Validation> validations;
    private final Pattern resourcePattern;
    private final Pattern parameterPattern;
    private final Pattern headerPattern;
    private RamlViolations violations;
    private Locator locator;

    private RamlValidator(Raml raml, List<SchemaValidator> schemaValidators, EnumSet<Validation> validations, Pattern resourcePattern, Pattern parameterPattern, Pattern headerPattern) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.validations = validations;
        this.resourcePattern = resourcePattern;
        this.parameterPattern = parameterPattern;
        this.headerPattern = headerPattern;
    }

    public RamlValidator(Raml raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, EnumSet.allOf(Validation.class), null, null, null);
    }

    public RamlValidator withChecks(Validation... validations) {
        final EnumSet<Validation> validationSet = validations.length == 0 ? EnumSet.noneOf(Validation.class) : EnumSet.copyOf(Arrays.asList(validations));
        return new RamlValidator(raml, schemaValidators, validationSet, resourcePattern, parameterPattern, headerPattern);
    }

    public RamlValidator withResourcePattern(String regex) {
        return new RamlValidator(raml, schemaValidators, validations, Pattern.compile(regex), parameterPattern, headerPattern);
    }

    public RamlValidator withParameterPattern(String regex) {
        return new RamlValidator(raml, schemaValidators, validations, resourcePattern, Pattern.compile(regex), headerPattern);
    }

    public RamlValidator withHeaderPattern(String regex) {
        return new RamlValidator(raml, schemaValidators, validations, resourcePattern, parameterPattern, Pattern.compile(regex));
    }

    public RamlReport validate() {
        final RamlReport report = new RamlReport(raml);
        violations = report.getValidationViolations();
        locator = new Locator();
        checkBaseUriParameters(raml.getBaseUriParameters().keySet());
        checkParameters(raml.getBaseUriParameters(), ParamName.BASE_URI);
        checkDescription(raml.getDocumentation());
        checkDescription(raml.getBaseUriParameters(), ParamName.BASE_URI);
        for (Resource resource : raml.getResources().values()) {
            validateResource(resource);
        }
        return report;
    }

    private void validateResource(Resource resource) {
        locator.resource(resource);
        checkResourcePattern(resource);
        checkBaseUriParameters(resource.getBaseUriParameters().keySet());
        checkUriParameters(resource.getUriParameters().keySet(), resource);
        checkParameters(resource.getBaseUriParameters(), ParamName.BASE_URI);
        checkParameters(resource.getUriParameters(), ParamName.URI);
        checkDescription(resource.getDescription());
        checkDescription(resource.getBaseUriParameters(), ParamName.BASE_URI);
        checkDescription(resource.getUriParameters(), ParamName.URI);
        for (Resource res : resource.getResources().values()) {
            validateResource(res);
        }
        for (Action action : resource.getActions().values()) {
            validateAction(action);
        }
    }

    private void validateAction(Action action) {
        locator.action(action);
        checkBaseUriParameters(action.getBaseUriParameters().keySet());
        checkParameters(action.getBaseUriParameters(), ParamName.BASE_URI);
        checkParameters(action.getQueryParameters(), ParamName.QUERY);
        checkHeaderPattern(action.getHeaders().keySet());
        checkDescription(action.getDescription());
        checkDescription(action.getBaseUriParameters(), ParamName.BASE_URI);
        checkDescription(action.getQueryParameters(), ParamName.QUERY);
        checkDescription(action.getHeaders(), ParamName.HEADER);
        if (action.getBody() != null) {
            for (MimeType mimeType : action.getBody().values()) {
                locator.requestMime(mimeType);
                validateMimeType(mimeType);
            }
        }
        for (Map.Entry<String, Response> entry : action.getResponses().entrySet()) {
            locator.responseCode(entry.getKey());
            validateResponse(entry.getValue());
        }
    }

    private void validateMimeType(MimeType mimeType) {
        if (mimeType.getFormParameters() != null) {
            if (!MediaType.valueOf(mimeType.getType()).isCompatibleWith(MediaType.FORM_URL_ENCODED) &&
                    !MediaType.valueOf(mimeType.getType()).isCompatibleWith(MediaType.MULTIPART)) {
                violations.add(new Message("formParameter.illegal", locator));
            }
            checkParameters(mimeType.getFormParameters(), ParamName.FORM);
            checkDescription(mimeType.getFormParameters(), ParamName.FORM);
        }
        checkExampleSchema(mimeType);
    }

    private void validateResponse(Response response) {
        checkHeaderPattern(response.getHeaders().keySet());
        checkDescription(response.getDescription());
        checkDescription(response.getHeaders(), ParamName.HEADER);
        if (response.getBody() != null) {
            for (MimeType mimeType : response.getBody().values()) {
                locator.responseMime(mimeType);
                validateMimeType(mimeType);
            }
        }
    }

    private void checkDescription(Map<String, ?> params, ParamName paramName) {
        if (validations.contains(Validation.DESCRIPTION)) {
            for (final Map.Entry<String, AbstractParam> param : paramEntries(params)) {
                checkDescription(param.getKey(), param.getValue(), paramName);
            }
        }
    }

    private void checkDescription(String name, AbstractParam param, ParamName paramName) {
        if (param.getDescription() == null || param.getDescription().isEmpty()) {
            violations.add(new Message("parameter.description.missing", locator, name, paramName));
        }
    }

    private void checkDescription(String desc) {
        if (validations.contains(Validation.DESCRIPTION)) {
            if (desc == null || desc.isEmpty()) {
                violations.add(new Message("description.missing", locator));
            }
        }
    }

    private void checkDescription(List<DocumentationItem> docs) {
        if (validations.contains(Validation.DESCRIPTION)) {
            if (docs == null || docs.isEmpty()) {
                violations.add(new Message("description.missing", locator));
            }
        }
    }

    private void checkBaseUriParameters(Collection<String> names) {
        if (validations.contains(Validation.URI_PARAMETER)) {
            if (raml.getBaseUri() == null && !names.isEmpty()) {
                violations.add(new Message("baseUriParameters.illegal", locator));
            } else {
                for (String name : names) {
                    if (name.equals("version")) {
                        violations.add(new Message("baseUriParameter.illegal", locator, name));
                    } else {
                        violations.addIf(!raml.getBaseUri().contains("{" + name + "}"), new Message("baseUriParameter.invalid", locator, name));
                    }
                }
            }
        }
    }

    private void checkUriParameters(Collection<String> names, Resource resource) {
        if (validations.contains(Validation.URI_PARAMETER)) {
            for (String name : names) {
                if (name.equals("version")) {
                    violations.add(new Message("uriParameter.illegal", locator, name));
                } else {
                    violations.addIf(!resource.getUri().contains("{" + name + "}"), new Message("uriParameter.invalid", locator, name));
                }
            }
        }
    }

    private void checkResourcePattern(Resource resource) {
        if (resourcePattern != null) {
            final String uri = resource.getRelativeUri().replaceAll("\\{[^}/]+\\}", "");
            for (final String part : uri.split("/")) {
                if (part != null && part.length() > 0 && !resourcePattern.matcher(part).matches()) {
                    violations.add(new Message("resource.name.invalid", locator, resourcePattern.pattern()));
                }
            }
        }
    }

    private void checkParameters(Map<String, ?> params, ParamName paramName) {
        if (parameterPattern != null) {
            for (final String name : params.keySet()) {
                if (!parameterPattern.matcher(name).matches()) {
                    violations.add(new Message("parameter.name.invalid", locator, name, paramName, parameterPattern.pattern()));
                }
            }
        }
        checkParameterDef(params, paramName);
        if (validations.contains(Validation.EXAMPLE)) {
            final ParameterChecker checker = new ParameterChecker(violations, false, false, false, null);
            for (final Map.Entry<String, AbstractParam> param : paramEntries(params)) {
                checkParameterValues(param.getValue(), checker, new Message("parameter.condition", locator, param.getKey(), paramName));
            }
        }
    }

    private void checkParameterDef(Map<String, ?> params, ParamName paramName) {
        for (final Map.Entry<String, AbstractParam> entry : paramEntries(params)) {
            final String name = entry.getKey();
            final AbstractParam param = entry.getValue();
            final ParamType type = param.getType() == null ? ParamType.STRING : param.getType();
            if (type == ParamType.STRING) {
                violations.addIf(param.getMinimum() != null, new Message("parameter.condition.illegal", locator, name, paramName, "minimum"));
                violations.addIf(param.getMaximum() != null, new Message("parameter.condition.illegal", locator, name, paramName, "maximum"));
            } else {
                violations.addIf(param.getEnumeration() != null, new Message("parameter.condition.illegal", locator, name, paramName, "enum"));
                violations.addIf(param.getPattern() != null, new Message("parameter.condition.illegal", locator, name, paramName, "pattern"));
                violations.addIf(param.getMinLength() != null, new Message("parameter.condition.illegal", locator, name, paramName, "minLength"));
                violations.addIf(param.getMaxLength() != null, new Message("parameter.condition.illegal", locator, name, paramName, "maxLength"));
                if (type != ParamType.INTEGER && type != ParamType.NUMBER) {
                    violations.addIf(param.getMinimum() != null, new Message("parameter.condition.illegal", locator, name, paramName, "minimum"));
                    violations.addIf(param.getMaximum() != null, new Message("parameter.condition.illegal", locator, name, paramName, "maximum"));
                }
                if (type == ParamType.FILE) {
                    violations.addIf(paramName != ParamName.FORM, new Message("parameter.file.illegal", locator, name, paramName));
                }
            }
        }
    }

    private void checkParameterValues(AbstractParam param, ParameterChecker checker, Message message) {
        if (param.getExample() != null) {
            checker.checkParameter(param, param.getExample(), message.withParam("example"));
        }
        if (param.getDefaultValue() != null) {
            checker.checkParameter(param, param.getDefaultValue(), message.withParam("default value"));
        }
    }

    private void checkHeaderPattern(Collection<String> names) {
        if (headerPattern != null) {
            for (final String name : names) {
                if (!headerPattern.matcher(name).matches()) {
                    violations.add(new Message("header.name.invalid", locator, name, headerPattern.pattern()));
                }
            }
        }
    }

    private void checkExampleSchema(MimeType mimeType) {
        if (validations.contains(Validation.EXAMPLE)) {
            final SchemaValidator validator = CheckerHelper.findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.getType()));
            if (mimeType.getExample() != null && validator != null) {
                final String schema = mimeType.getSchema();
                final String refSchema = raml.getConsolidatedSchemas().get(schema);
                validator.validate(mimeType.getExample(), refSchema != null ? refSchema : schema, violations,
                        new Message("schema.example.mismatch", locator, mimeType.getExample()));
            }
        }
    }
}
