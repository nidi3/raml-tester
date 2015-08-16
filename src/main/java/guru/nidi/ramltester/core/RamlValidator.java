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

/**
 *
 */
public class RamlValidator {
    public enum Validation {
        URI_PARAMETER, EXAMPLE_SCHEMA, DESCRIPTION
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
        checkParameterPattern(raml.getBaseUriParameters().keySet(), "baseUriParameter");
        checkDescription(raml.getDocumentation());
        checkDescription(raml.getBaseUriParameters(), "baseUriParameter");
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
        checkParameterPattern(resource.getBaseUriParameters().keySet(), "baseUriParameter");
        checkParameterPattern(resource.getUriParameters().keySet(), "uriParameter");
        checkDescription(resource.getDescription());
        checkDescription(resource.getBaseUriParameters(), "baseUriParameter");
        checkDescription(resource.getUriParameters(), "uriParameter");
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
        checkParameterPattern(action.getBaseUriParameters().keySet(), "baseUriParameter");
        checkParameterPattern(action.getQueryParameters().keySet(), "queryParameter");
        checkHeaderPattern(action.getHeaders().keySet());
        checkDescription(action.getDescription());
        checkDescription(action.getBaseUriParameters(), "baseUriParameter");
        checkDescription(action.getQueryParameters(), "queryParameter");
        checkDescription(action.getHeaders(), "header");
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
            checkParameterPattern(mimeType.getFormParameters().keySet(), "formParameter");
            checkDescription(mimeType.getFormParameters(), "formParameter");
        }
        checkExampleSchema(mimeType);
    }

    private void validateResponse(Response response) {
        checkHeaderPattern(response.getHeaders().keySet());
        checkDescription(response.getDescription());
        checkDescription(response.getHeaders(), "header");
        if (response.getBody() != null) {
            for (MimeType mimeType : response.getBody().values()) {
                locator.responseMime(mimeType);
                validateMimeType(mimeType);
            }
        }
    }

    private void checkDescription(Map<String, ?> params, String paramType) {
        if (!validations.contains(Validation.DESCRIPTION)) {
            return;
        }
        for (final Map.Entry<String, ?> param : params.entrySet()) {
            if (param.getValue() instanceof List) {
                for (final AbstractParam elem : (List<AbstractParam>) param.getValue()) {
                    checkDescription(param.getKey(), elem, paramType);
                }
            } else {
                checkDescription(param.getKey(), (AbstractParam) param.getValue(), paramType);
            }
        }
    }

    private void checkDescription(String name, AbstractParam param, String paramType) {
        if (param.getDescription() == null || param.getDescription().isEmpty()) {
            violations.add(new Message("parameter.description.missing", locator, name, paramType));
        }
    }

    private void checkDescription(String desc) {
        if (!validations.contains(Validation.DESCRIPTION)) {
            return;
        }
        if (desc == null || desc.isEmpty()) {
            violations.add(new Message("description.missing", locator));
        }
    }

    private void checkDescription(List<DocumentationItem> docs) {
        if (!validations.contains(Validation.DESCRIPTION)) {
            return;
        }
        if (docs == null || docs.isEmpty()) {
            violations.add(new Message("description.missing", locator));
        }
    }

    private void checkBaseUriParameters(Collection<String> names) {
        if (!validations.contains(Validation.URI_PARAMETER)) {
            return;
        }
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

    private void checkUriParameters(Collection<String> names, Resource resource) {
        if (!validations.contains(Validation.URI_PARAMETER)) {
            return;
        }
        for (String name : names) {
            if (name.equals("version")) {
                violations.add(new Message("uriParameter.illegal", locator, name));
            } else {
                violations.addIf(!resource.getUri().contains("{" + name + "}"), new Message("uriParameter.invalid", locator, name));
            }
        }
    }

    private void checkResourcePattern(Resource resource) {
        if (resourcePattern == null) {
            return;
        }
        final String uri = resource.getRelativeUri().replaceAll("\\{[^}/]+\\}", "");
        for (final String part : uri.split("/")) {
            if (part != null && part.length() > 0 && !resourcePattern.matcher(part).matches()) {
                violations.add(new Message("resource.name.invalid", locator, resourcePattern.pattern()));
            }
        }
    }

    private void checkParameterPattern(Collection<String> names, String what) {
        if (parameterPattern == null) {
            return;
        }
        for (final String name : names) {
            if (!parameterPattern.matcher(name).matches()) {
                violations.add(new Message("parameter.name.invalid", locator, name, what, parameterPattern.pattern()));
            }
        }
    }

    private void checkHeaderPattern(Collection<String> names) {
        if (headerPattern == null) {
            return;
        }
        for (final String name : names) {
            if (!headerPattern.matcher(name).matches()) {
                violations.add(new Message("header.name.invalid", locator, name, headerPattern.pattern()));
            }
        }
    }

    private void checkExampleSchema(MimeType mimeType) {
        if (!validations.contains(Validation.EXAMPLE_SCHEMA)) {
            return;
        }
        final SchemaValidator validator = CheckerHelper.findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.getType()));
        if (mimeType.getExample() != null && validator != null) {
            final String schema = mimeType.getSchema();
            final String refSchema = raml.getConsolidatedSchemas().get(schema);
            validator.validate(mimeType.getExample(), refSchema != null ? refSchema : schema, violations,
                    new Message("schema.example.mismatch", locator, mimeType.getExample()));
        }
    }
}
