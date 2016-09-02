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

import guru.nidi.ramltester.model.*;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.v2.api.model.v08.parameters.NumberTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.StringTypeDeclaration;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.core.CheckerHelper.*;
import static guru.nidi.ramltester.model.UnifiedModel.typeNamesOf;

/**
 *
 */
class RamlValidatorChecker {

    private static final String PARAM_CONDITION_ILLEGAL = "parameter.condition.illegal";

    enum ParamName {
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

    private final UnifiedApi raml;
    private final Locator locator;
    private final Pattern resourcePattern;
    private final Pattern parameterPattern;
    private final Pattern headerPattern;
    private final List<SchemaValidator> schemaValidators;
    private final EnumSet<Validation> validations;
    private final RamlReport report;
    private final RamlViolations violations;

    public RamlValidatorChecker(UnifiedApi raml, List<SchemaValidator> schemaValidators) {
        this(raml, new Locator(), schemaValidators, EnumSet.allOf(Validation.class), null, null, null);
    }

    public RamlValidatorChecker(UnifiedApi raml, Locator locator, List<SchemaValidator> schemaValidators, EnumSet<Validation> validations, Pattern resourcePattern, Pattern parameterPattern, Pattern headerPattern) {
        this.raml = raml;
        this.locator = locator;
        this.resourcePattern = resourcePattern;
        this.parameterPattern = parameterPattern;
        this.headerPattern = headerPattern;
        this.schemaValidators = schemaValidators;
        this.validations = validations;
        this.report = new RamlReport(raml);
        this.violations = report.getValidationViolations();
    }

    public RamlValidatorChecker withChecks(Validation... validations) {
        final EnumSet<Validation> validationSet = validations.length == 0 ? EnumSet.noneOf(Validation.class) : EnumSet.copyOf(Arrays.asList(validations));
        return new RamlValidatorChecker(raml, locator, schemaValidators, validationSet, headerPattern, parameterPattern, resourcePattern);
    }

    public RamlValidatorChecker withResourcePattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, regex == null ? null : Pattern.compile(regex), parameterPattern, headerPattern);
    }

    public RamlValidatorChecker withParameterPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern, regex == null ? null : Pattern.compile(regex), headerPattern);
    }

    public RamlValidatorChecker withHeaderPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern, parameterPattern, regex == null ? null : Pattern.compile(regex));
    }

    public Locator getLocator() {
        return locator;
    }

    public RamlReport getReport() {
        return report;
    }

    private boolean has(Validation validation) {
        return validations.contains(validation);
    }

    private void violation(String key, Object... params) {
        violations.add(new Message(key, params));
    }

    public void description(List<UnifiedType> params, ParamName paramName) {
        if (has(Validation.DESCRIPTION)) {
            for (final UnifiedType param : paramEntries(params)) {
                description(param.name(), param, paramName);
            }
        }
    }

    private void description(String name, UnifiedType param, ParamName paramName) {
        if (isNullOrEmpty(param.description())) {
            violation("parameter.description.missing", locator, name, paramName);
        }
    }

    public void description(String desc) {
        if (has(Validation.DESCRIPTION)) {
            if (isNullOrEmpty(desc)) {
                violation("description.missing", locator);
            }
        }
    }

    public void description(List<UnifiedDocItem> docs) {
        if (has(Validation.DESCRIPTION)) {
            if (isNullOrEmpty(docs)) {
                violation("documentation.missing", locator);
            } else {
                for (final UnifiedDocItem doc : docs) {
                    if (isNullOrEmpty(doc.title())) {
                        violation("documentation.missing.title", locator);
                    } else if (isNullOrEmpty(doc.content())) {
                        violation("documentation.missing.content", locator);
                    }
                }
            }
        }
    }

    public void empty(UnifiedResource resource) {
        if (has(Validation.EMPTY)) {
            if (resource.methods().isEmpty() && resource.resources().isEmpty()) {
                violation("empty", locator);
            }
        }
    }

    public void empty(UnifiedMethod action) {
        if (has(Validation.EMPTY)) {
            if (action.responses().isEmpty()) {
                violation("empty", locator);
            }
        }
    }

    private void baseUriParameters(Collection<String> names) {
        if (has(Validation.URI_PARAMETER)) {
            if (raml.baseUri() == null && !names.isEmpty()) {
                violation("baseUriParameters.illegal", locator);
            } else {
                for (final String name : names) {
                    if ("version".equals(name)) {
                        violation("baseUriParameter.illegal", locator, name);
                    } else if (!raml.baseUri().contains("{" + name + "}")) {
                        violation("baseUriParameter.invalid", locator, name);
                    }
                }
            }
        }
    }

    public void uriParameters(Collection<String> names, UnifiedResource resource) {
        if (has(Validation.URI_PARAMETER)) {
            for (final String name : names) {
                if ("version".equals(name)) {
                    violation("uriParameter.illegal", locator, name);
                } else if (!resource.resourcePath().contains("{" + name + "}")) {
                    violation("uriParameter.invalid", locator, name);
                }
            }
        }
    }

    public void resourcePattern(UnifiedResource resource) {
        if (resourcePattern != null) {
            final String uri = resource.relativeUri().replaceAll("\\{[^}/]+\\}", "");
            for (final String part : uri.split("/")) {
                if (part != null && part.length() > 0 && !resourcePattern.matcher(part).matches()) {
                    violation("resource.name.invalid", locator, resourcePattern.pattern());
                }
            }
        }
    }

    public void parameters(List<UnifiedType> params, ParamName paramName) {
        if (paramName == ParamName.BASE_URI) {
            baseUriParameters(typeNamesOf(params));
        }
        if (parameterPattern != null) {
            for (final UnifiedType param : params) {
                if (!parameterPattern.matcher(param.name()).matches()) {
                    violation("parameter.name.invalid", locator, param.name(), paramName, parameterPattern.pattern());
                }
            }
        }
        if (has(Validation.PARAMETER)) {
            parameterDef(params, paramName);
        }
        if (has(Validation.EXAMPLE)) {
//            final ParameterChecker08 checker = new ParameterChecker08(violations);
            for (final UnifiedType param : paramEntries(params)) {
                parameterValues(param, new Message("parameter.condition", locator, param.name(), paramName));
            }
        }
    }

    public void formParameters(UnifiedBody mimeType) {
        if (has(Validation.PARAMETER)) {
            if (!MediaType.valueOf(mimeType.name()).isCompatibleWith(MediaType.FORM_URL_ENCODED) &&
                    !MediaType.valueOf(mimeType.name()).isCompatibleWith(MediaType.MULTIPART)) {
                violation("formParameter.illegal", locator);
            }
        }
    }

    private void parameterDef(List<UnifiedType> params, ParamName paramName) {
//        for (final Parameter param : paramEntries(params)) {
//            final String name = param.name();
//            final String type = param.type() == null ? "string": param.type();
//            if (param instanceof StringTypeDeclaration) {
//                minMaxNotAllowed((NumberTypeDeclaration) param, name, paramName);
//            } else {
//                stringConstraintsNotAllowed(param, name, paramName);
//                if (type != ParamType.INTEGER && type != ParamType.NUMBER) {
//                    minMaxNotAllowed(param, name, paramName);
//                }
//                if (type == ParamType.FILE) {
//                    violations.addIf(paramName != ParamName.FORM, new Message("parameter.file.illegal", locator, name, paramName));
//                }
//            }
//        }
    }

    private void stringConstraintsNotAllowed(StringTypeDeclaration param, String name, ParamName paramName) {
        violations.addIf(param.enumValues() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "enum"));
        violations.addIf(param.pattern() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "pattern"));
        violations.addIf(param.minLength() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "minLength"));
        violations.addIf(param.maxLength() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "maxLength"));
    }

    private void minMaxNotAllowed(NumberTypeDeclaration param, String name, ParamName paramName) {
        violations.addIf(param.minimum() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "minimum"));
        violations.addIf(param.maximum() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "maximum"));
    }

    private void parameterValues(UnifiedType param,  Message message) {
        if (!param.examples().isEmpty()) {
            param.validate(param.examples(), violations, message.withParam("example"));
//            checker.checkParameter(param.<Parameter>delegate(), param.examples(), message.withParam("example"));
        }
        if (param.defaultValue() != null) {
            param.validate(param.defaultValue(), violations, message.withParam("default value"));
//            checker.checkParameter(param.<Parameter>delegate(), param.defaultValue(), message.withParam("default value"));
        }
    }

    public void headerPattern(Collection<String> names) {
        if (headerPattern != null) {
            for (final String name : names) {
                if (!headerPattern.matcher(name).matches()) {
                    violation("header.name.invalid", locator, name, headerPattern.pattern());
                }
            }
        }
    }

    public void exampleSchema(UnifiedBody mimeType) {
        if (has(Validation.EXAMPLE)) {
            final String schema = mimeType.type();
            final SchemaValidator validator = findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.name()));
            if (schema != null && validator != null) {
                for (final String example : mimeType.examples()) {
                    validator.validate(new NamedReader(example, new Message("example").toString()), resolveSchema(raml, schema), violations,
                            new Message("schema.example.mismatch", locator, example));
                }
            }
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean isNullOrEmpty(List<?> s) {
        return s == null || s.isEmpty();
    }
}
