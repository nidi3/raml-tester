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

import java.util.*;
import java.util.regex.Pattern;

/**
 *
 */
public class RamlValidator {
    public enum Validation {
        URI_PARAMETER, EXAMPLE_SCHEMA
    }

    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final EnumSet<Validation> validations;
    private final Pattern resourcePattern;
    private final Pattern parameterPattern;
    private final Pattern headerPattern;
    private RamlViolations violations;

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
        checkBaseUriParameters(raml.getBaseUriParameters().keySet(), new Message("root").toString());
        checkParameterPattern(raml.getBaseUriParameters().keySet(), new Message("root").toString(), "baseUriParameter");
        for (Resource resource : raml.getResources().values()) {
            validateResource(resource);
        }
        return report;
    }

    private void validateResource(Resource resource) {
        checkResourcePattern(resource);
        checkBaseUriParameters(resource.getBaseUriParameters().keySet(), resource);
        checkUriParameters(resource.getUriParameters().keySet(), resource);
        checkParameterPattern(resource.getBaseUriParameters().keySet(), resource, "baseUriParameter");
        checkParameterPattern(resource.getUriParameters().keySet(), resource, "uriParameter");
        for (Resource res : resource.getResources().values()) {
            validateResource(res);
        }
        for (Action action : resource.getActions().values()) {
            validateAction(action);
        }
    }

    private void validateAction(Action action) {
        checkBaseUriParameters(action.getBaseUriParameters().keySet(), action);
        checkParameterPattern(action.getBaseUriParameters().keySet(), action, "baseUriParameter");
        checkParameterPattern(action.getQueryParameters().keySet(), action, "queryParameter");
        checkHeaderPattern(action.getHeaders().keySet(), action, "");
        if (action.getBody() != null) {
            for (MimeType mimeType : action.getBody().values()) {
                validateMimeType(action, mimeType, "");
            }
        }
        for (Map.Entry<String, Response> entry : action.getResponses().entrySet()) {
            validateResponse(action, entry.getKey(), entry.getValue());
        }
    }

    private void validateMimeType(Action action, MimeType mimeType, String detail) {
        if (mimeType.getFormParameters() != null) {
            checkParameterPattern(mimeType.getFormParameters().keySet(), new Message("triple", action, mimeType, detail), "formParameter");
        }
        checkExampleSchema(action, mimeType, detail);
    }

    private void validateResponse(Action action, String code, Response response) {
        checkHeaderPattern(response.getHeaders().keySet(), action, new Message("response", code).toString());
        if (response.getBody() != null) {
            final String detail = new Message("response", code).toString();
            for (MimeType mimeType : response.getBody().values()) {
                validateMimeType(action, mimeType, detail);
            }
        }
    }

    private void checkBaseUriParameters(Collection<String> names, Object detail) {
        if (!validations.contains(Validation.URI_PARAMETER)) {
            return;
        }
        if (raml.getBaseUri() == null && !names.isEmpty()) {
            violations.add(new Message("baseUriParameters.illegal", detail));
        } else {
            for (String name : names) {
                if (name.equals("version")) {
                    violations.add(new Message("baseUriParameter.illegal", name, detail));
                } else {
                    violations.addIf(!raml.getBaseUri().contains("{" + name + "}"), new Message("baseUriParameter.invalid", name, detail));
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
                violations.add(new Message("uriParameter.illegal", name, resource));
            } else {
                violations.addIf(!resource.getUri().contains("{" + name + "}"), new Message("uriParameter.invalid", name, resource));
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
                violations.add(new Message("resource.name.invalid", resource, resourcePattern.pattern()));
            }
        }
    }

    private void checkParameterPattern(Collection<String> names, Object where, String what) {
        if (parameterPattern == null) {
            return;
        }
        for (final String name : names) {
            if (!parameterPattern.matcher(name).matches()) {
                violations.add(new Message("parameter.name.invalid", name, where, what, parameterPattern.pattern()));
            }
        }
    }

    private void checkHeaderPattern(Collection<String> names, Object where, String detail) {
        if (headerPattern == null) {
            return;
        }
        for (final String name : names) {
            if (!headerPattern.matcher(name).matches()) {
                violations.add(new Message("header.name.invalid", name, where, detail, headerPattern.pattern()));
            }
        }
    }

    private void checkExampleSchema(Action action, MimeType mimeType, String detail) {
        if (!validations.contains(Validation.EXAMPLE_SCHEMA)) {
            return;
        }
        final SchemaValidator validator = CheckerHelper.findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.getType()));
        if (mimeType.getExample() != null && validator != null) {
            final String schema = mimeType.getSchema();
            final String refSchema = raml.getConsolidatedSchemas().get(schema);
            validator.validate(mimeType.getExample(), refSchema != null ? refSchema : schema, violations,
                    new Message("schema.example.mismatch", action, detail, mimeType, mimeType.getExample()));
        }
    }
}
