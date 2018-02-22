/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.internal.*;

import java.util.List;

import static guru.nidi.ramltester.core.CheckerHelper.typeNamesOf;
import static guru.nidi.ramltester.core.RamlValidatorChecker.ParamName.*;

public class RamlValidator {
    private final RamlApi raml;
    private final List<SchemaValidator> schemaValidators;
    private final Locator locator;
    private final RamlValidatorChecker checker;

    public RamlValidator(RamlApi raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, new RamlValidatorChecker(raml, schemaValidators));
    }

    private RamlValidator(RamlApi raml, List<SchemaValidator> schemaValidators, RamlValidatorChecker checker) {
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
        checker.parameters(raml.annotationTypes(), ANNOTATION);
        checker.description(raml.documentation());
        checker.description(raml.description());
        checker.description(raml.baseUriParameters(), BASE_URI);
        checker.description(raml.annotationTypes(), ANNOTATION);
        for (final RamlResource resource : raml.resources()) {
            resource(resource);
        }
        return checker.getReport();
    }

    private void resource(RamlResource resource) {
        locator.resource(resource);
        checker.resourcePattern(resource);
        checker.uriParameters(typeNamesOf(resource.uriParameters()), resource);
        checker.parameters(resource.baseUriParameters(), BASE_URI);
        checker.parameters(resource.uriParameters(), URI);
        checker.description(resource.description());
        checker.description(resource.baseUriParameters(), BASE_URI);
        checker.description(resource.uriParameters(), URI);
        checker.empty(resource);
        for (final RamlResource res : resource.resources()) {
            resource(res);
        }
        for (final RamlMethod method : resource.methods()) {
            method(method);
        }
    }

    private void method(RamlMethod method) {
        locator.method(method);
        checker.parameters(method.baseUriParameters(), BASE_URI);
        checker.parameters(method.queryParameters(), QUERY);
        checker.headerPattern(typeNamesOf(method.headers()));
        checker.description(method.description());
        checker.description(method.baseUriParameters(), BASE_URI);
        checker.description(method.queryParameters(), QUERY);
        checker.description(method.headers(), HEADER);
        checker.empty(method);
        if (method.body() != null) {
            for (final RamlBody body : method.body()) {
                locator.requestBody(body);
                body(body);
            }
        }
        for (final RamlApiResponse response : method.responses()) {
            locator.responseCode(response.code());
            response(response);
        }
    }

    private void body(RamlBody body) {
        if (!body.formParameters().isEmpty()) {
            checker.formParameters(body);
            checker.parameters(body.formParameters(), FORM);
            checker.description(body.formParameters(), FORM);
        }
        checker.exampleSchema(body);
    }

    private void response(RamlApiResponse response) {
        checker.headerPattern(typeNamesOf(response.headers()));
        checker.description(response.description());
        checker.description(response.headers(), HEADER);
        if (response.body() != null) {
            for (final RamlBody body : response.body()) {
                locator.responseBody(body);
                body(body);
            }
        }
    }

}
