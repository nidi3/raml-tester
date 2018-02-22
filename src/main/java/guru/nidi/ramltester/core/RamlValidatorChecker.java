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

import guru.nidi.ramltester.model.internal.*;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;

import java.util.*;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.core.CheckerHelper.*;

class RamlValidatorChecker {
    enum ParamName {
        BASE_URI("baseUriParameter"),
        URI("uriParameter"),
        QUERY("queryParameter"),
        HEADER("header"),
        FORM("formParameter"),
        ANNOTATION("annotationType");
        private final String value;

        ParamName(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private final RamlApi raml;
    private final Locator locator;
    private final Pattern resourcePattern;
    private final Pattern parameterPattern;
    private final Pattern headerPattern;
    private final List<SchemaValidator> schemaValidators;
    private final EnumSet<Validation> validations;
    private final RamlReport report;
    private final RamlViolations violations;

    public RamlValidatorChecker(RamlApi raml, List<SchemaValidator> schemaValidators) {
        this(raml, new Locator(), schemaValidators, EnumSet.allOf(Validation.class), null, null, null);
    }

    public RamlValidatorChecker(RamlApi raml, Locator locator, List<SchemaValidator> schemaValidators,
                                EnumSet<Validation> validations, Pattern resourcePattern, Pattern parameterPattern,
                                Pattern headerPattern) {
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
        final EnumSet<Validation> validationSet = validations.length == 0
                ? EnumSet.noneOf(Validation.class)
                : EnumSet.copyOf(Arrays.asList(validations));
        return new RamlValidatorChecker(raml, locator, schemaValidators, validationSet, headerPattern, parameterPattern,
                resourcePattern);
    }

    public RamlValidatorChecker withResourcePattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations,
                regex == null ? null : Pattern.compile(regex), parameterPattern, headerPattern);
    }

    public RamlValidatorChecker withParameterPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern,
                regex == null ? null : Pattern.compile(regex), headerPattern);
    }

    public RamlValidatorChecker withHeaderPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern, parameterPattern,
                regex == null ? null : Pattern.compile(regex));
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

    public void description(List<RamlType> params, ParamName paramName) {
        if (has(Validation.DESCRIPTION)) {
            for (final RamlType param : params) {
                description(param.name(), param, paramName);
            }
        }
    }

    private void description(String name, RamlType param, ParamName paramName) {
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

    public void description(List<RamlDocItem> docs) {
        if (has(Validation.DESCRIPTION)) {
            if (isNullOrEmpty(docs)) {
                violation("documentation.missing", locator);
            } else {
                for (final RamlDocItem doc : docs) {
                    if (isNullOrEmpty(doc.title())) {
                        violation("documentation.missing.title", locator);
                    } else if (isNullOrEmpty(doc.content())) {
                        violation("documentation.missing.content", locator);
                    }
                }
            }
        }
    }

    public void empty(RamlResource resource) {
        if (has(Validation.EMPTY)) {
            if (resource.methods().isEmpty() && resource.resources().isEmpty()) {
                violation("empty", locator);
            }
        }
    }

    public void empty(RamlMethod method) {
        if (has(Validation.EMPTY)) {
            if (method.responses().isEmpty()) {
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

    public void uriParameters(Collection<String> names, RamlResource resource) {
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

    public void resourcePattern(RamlResource resource) {
        if (resourcePattern != null) {
            final String uri = resource.relativeUri().replaceAll("\\{[^}/]+}", "");
            for (final String part : uri.split("/")) {
                if (part != null && part.length() > 0 && !resourcePattern.matcher(part).matches()) {
                    violation("resource.name.invalid", locator, resourcePattern.pattern());
                }
            }
        }
    }

    public void parameters(List<RamlType> params, ParamName paramName) {
        if (paramName == ParamName.BASE_URI) {
            baseUriParameters(typeNamesOf(params));
        }
        if (parameterPattern != null) {
            for (final RamlType param : params) {
                if (!parameterPattern.matcher(param.name()).matches()) {
                    violation("parameter.name.invalid", locator, param.name(), paramName, parameterPattern.pattern());
                }
            }
        }
        if (has(Validation.EXAMPLE)) {
            for (final RamlType param : params) {
                parameterValues(param, new Message("parameter.condition", locator, param.name(), paramName));
            }
        }
    }

    public void formParameters(RamlBody body) {
        if (has(Validation.PARAMETER)) {
            if (!MediaType.valueOf(body.name()).isCompatibleWith(MediaType.FORM_URL_ENCODED)
                    && !MediaType.valueOf(body.name()).isCompatibleWith(MediaType.MULTIPART)) {
                violation("formParameter.illegal", locator);
            }
        }
    }

    private void parameterValues(RamlType param, Message message) {
        final TypeChecker checker = new TypeChecker(violations);
        for (final String example : param.examples()) {
            checker.check(param, example, message.withParam("example"));
        }
        if (param.defaultValue() != null) {
            checker.check(param, param.defaultValue(), message.withParam("default value"));
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

    public void exampleSchema(RamlBody body) {
        if (has(Validation.EXAMPLE)) {
            final String typeDef = body.typeDefinition();
            final String type = body.type();
            final SchemaValidator validator = findSchemaValidator(schemaValidators, MediaType.valueOf(body.name()));
            if ((typeDef != null || type != null) && validator != null) {
                for (final String example : body.examples()) {
                    validator.validate(new NamedReader(example, new Message("example").toString()),
                            resolveSchema(type, typeDef), violations, new Message("schema.example.mismatch", locator, example));
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
