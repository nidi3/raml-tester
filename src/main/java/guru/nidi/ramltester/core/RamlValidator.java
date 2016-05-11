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

import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.bodies.BodyLike;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.resources.Resource;

import java.util.List;

import static guru.nidi.ramltester.core.CheckerHelper.namesOf;
import static guru.nidi.ramltester.core.RamlValidatorChecker.ParamName.*;

/**
 *
 */
public class RamlValidator {
    private final Api raml;
    private final List<SchemaValidator> schemaValidators;
    private final Locator locator;
    private final RamlValidatorChecker checker;

    public RamlValidator(Api raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, new RamlValidatorChecker(raml, schemaValidators));
    }

    private RamlValidator(Api raml, List<SchemaValidator> schemaValidators, RamlValidatorChecker checker) {
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
        for (final Resource resource : raml.resources()) {
            resource(resource);
        }
        return checker.getReport();
    }

    private void resource(Resource resource) {
        locator.resource(resource);
        checker.resourcePattern(resource);
        checker.uriParameters(namesOf(resource.uriParameters()), resource);
        checker.parameters(resource.baseUriParameters(), BASE_URI);
        checker.parameters(resource.uriParameters(), URI);
        checker.description(resource.description());
        checker.description(resource.baseUriParameters(), BASE_URI);
        checker.description(resource.uriParameters(), URI);
        checker.empty(resource);
        for (final Resource res : resource.resources()) {
            resource(res);
        }
        for (final Method action : resource.methods()) {
            action(action);
        }
    }

    private void action(Method action) {
        locator.action(action);
        checker.parameters(action.baseUriParameters(), BASE_URI);
        checker.parameters(action.queryParameters(), QUERY);
        checker.headerPattern(namesOf(action.headers()));
        checker.description(action.description());
        checker.description(action.baseUriParameters(), BASE_URI);
        checker.description(action.queryParameters(), QUERY);
        checker.description(action.headers(), HEADER);
        checker.empty(action);
        if (action.body() != null) {
            for (final BodyLike mimeType : action.body()) {
                locator.requestMime(mimeType);
                mimeType(mimeType);
            }
        }
        for (final Response response : action.responses()) {
            locator.responseCode(response.code().value());
            response(response);
        }
    }

    private void mimeType(BodyLike mimeType) {
        if (mimeType.formParameters() != null) {
            checker.formParameters(mimeType);
            checker.parameters(mimeType.formParameters(), FORM);
            checker.description(mimeType.formParameters(), FORM);
        }
        checker.exampleSchema(mimeType);
    }

    private void response(Response response) {
        checker.headerPattern(namesOf(response.headers()));
        checker.description(response.description());
        checker.description(response.headers(), HEADER);
        if (response.body() != null) {
            for (final BodyLike mimeType : response.body()) {
                locator.responseMime(mimeType);
                mimeType(mimeType);
            }
        }
    }

}
