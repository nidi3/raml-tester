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

import java.util.List;

import static guru.nidi.ramltester.core.RamlValidatorChecker.ParamName.*;
import static guru.nidi.ramltester.model.UnifiedModel.typeNamesOf;

public class RamlValidator {
    private final UnifiedApi raml;
    private final List<SchemaValidator> schemaValidators;
    private final Locator locator;
    private final RamlValidatorChecker checker;

    public RamlValidator(UnifiedApi raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, new RamlValidatorChecker(raml, schemaValidators));
    }

    private RamlValidator(UnifiedApi raml, List<SchemaValidator> schemaValidators, RamlValidatorChecker checker) {
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
        checker.parameters(raml.baseUriParameters(), BASE_URI);
        checker.description(raml.documentation());
        checker.description(raml.baseUriParameters(), BASE_URI);
        for (final UnifiedResource resource : raml.resources()) {
            resource(resource);
        }
        return checker.getReport();
    }

    private void resource(UnifiedResource resource) {
        locator.resource(resource);
        checker.resourcePattern(resource);
        checker.uriParameters(typeNamesOf(resource.uriParameters()), resource);
        checker.parameters(resource.baseUriParameters(), BASE_URI);
        checker.parameters(resource.uriParameters(), URI);
        checker.description(resource.description());
        checker.description(resource.baseUriParameters(), BASE_URI);
        checker.description(resource.uriParameters(), URI);
        checker.empty(resource);
        for (final UnifiedResource res : resource.resources()) {
            resource(res);
        }
        for (final UnifiedMethod action : resource.methods()) {
            action(action);
        }
    }

    private void action(UnifiedMethod action) {
        locator.action(action);
        checker.parameters(action.baseUriParameters(), BASE_URI);
        checker.parameters(action.queryParameters(), QUERY);
        checker.headerPattern(typeNamesOf(action.headers()));
        checker.description(action.description());
        checker.description(action.baseUriParameters(), BASE_URI);
        checker.description(action.queryParameters(), QUERY);
        checker.description(action.headers(), HEADER);
        checker.empty(action);
        if (action.body() != null) {
            for (final UnifiedBody mimeType : action.body()) {
                locator.requestMime(mimeType);
                mimeType(mimeType);
            }
        }
        for (final UnifiedResponse response : action.responses()) {
            locator.responseCode(response.code());
            response(response);
        }
    }

    private void mimeType(UnifiedBody mimeType) {
        if (!mimeType.formParameters().isEmpty()) {
            checker.formParameters(mimeType);
            checker.parameters(mimeType.formParameters(), FORM);
            checker.description(mimeType.formParameters(), FORM);
        }
        checker.exampleSchema(mimeType);
    }

    private void response(UnifiedResponse response) {
        checker.headerPattern(typeNamesOf(response.headers()));
        checker.description(response.description());
        checker.description(response.headers(), HEADER);
        if (response.body() != null) {
            for (final UnifiedBody mimeType : response.body()) {
                locator.responseMime(mimeType);
                mimeType(mimeType);
            }
        }
    }

}
