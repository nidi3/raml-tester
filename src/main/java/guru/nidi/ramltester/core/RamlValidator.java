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

import org.raml.model.*;

import java.util.List;
import java.util.Map;

import static guru.nidi.ramltester.core.RamlValidatorChecker.ParamName.*;

/**
 *
 */
public class RamlValidator {
    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final Locator locator;
    private final RamlValidatorChecker checker;

    public RamlValidator(Raml raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, new RamlValidatorChecker(raml, schemaValidators));
    }

    private RamlValidator(Raml raml, List<SchemaValidator> schemaValidators, RamlValidatorChecker checker) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.checker = checker;
        this.locator = checker.getLocator();
    }

    public RamlValidator withChecks(Validation... validations) {
        return new RamlValidator(raml, schemaValidators, checker.withChecks(validations));
    }

    public RamlValidator withResourcePattern(String regex) {
        return new RamlValidator(raml, schemaValidators, checker.withResourcePattern(regex));
    }

    public RamlValidator withParameterPattern(String regex) {
        return new RamlValidator(raml, schemaValidators, checker.withParameterPattern(regex));
    }

    public RamlValidator withHeaderPattern(String regex) {
        return new RamlValidator(raml, schemaValidators, checker.withHeaderPattern(regex));
    }

    public RamlReport validate() {
        checker.parameters(raml.getBaseUriParameters(), BASE_URI);
        checker.description(raml.getDocumentation());
        checker.description(raml.getBaseUriParameters(), BASE_URI);
        for (Resource resource : raml.getResources().values()) {
            resource(resource);
        }
        return checker.getReport();
    }

    private void resource(Resource resource) {
        locator.resource(resource);
        checker.resourcePattern(resource);
        checker.uriParameters(resource.getUriParameters().keySet(), resource);
        checker.parameters(resource.getBaseUriParameters(), BASE_URI);
        checker.parameters(resource.getUriParameters(), URI);
        checker.description(resource.getDescription());
        checker.description(resource.getBaseUriParameters(), BASE_URI);
        checker.description(resource.getUriParameters(), URI);
        for (Resource res : resource.getResources().values()) {
            resource(res);
        }
        for (Action action : resource.getActions().values()) {
            action(action);
        }
    }

    private void action(Action action) {
        locator.action(action);
        checker.parameters(action.getBaseUriParameters(), BASE_URI);
        checker.parameters(action.getQueryParameters(), QUERY);
        checker.headerPattern(action.getHeaders().keySet());
        checker.description(action.getDescription());
        checker.description(action.getBaseUriParameters(), BASE_URI);
        checker.description(action.getQueryParameters(), QUERY);
        checker.description(action.getHeaders(), HEADER);
        if (action.getBody() != null) {
            for (MimeType mimeType : action.getBody().values()) {
                locator.requestMime(mimeType);
                mimeType(mimeType);
            }
        }
        for (Map.Entry<String, Response> entry : action.getResponses().entrySet()) {
            locator.responseCode(entry.getKey());
            response(entry.getValue());
        }
    }

    private void mimeType(MimeType mimeType) {
        if (mimeType.getFormParameters() != null) {
            checker.formParameters(mimeType);
            checker.parameters(mimeType.getFormParameters(), FORM);
            checker.description(mimeType.getFormParameters(), FORM);
        }
        checker.exampleSchema(mimeType);
    }

    private void response(Response response) {
        checker.headerPattern(response.getHeaders().keySet());
        checker.description(response.getDescription());
        checker.description(response.getHeaders(), HEADER);
        if (response.getBody() != null) {
            for (MimeType mimeType : response.getBody().values()) {
                locator.responseMime(mimeType);
                mimeType(mimeType);
            }
        }
    }

}
