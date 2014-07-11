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
package guru.nidi.ramltester;

import guru.nidi.ramltester.core.RamlChecker;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.servlet.ServletTester;
import guru.nidi.ramltester.spring.RamlMatcher;
import guru.nidi.ramltester.spring.RamlRestTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.raml.model.Raml;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 *
 */
public class RamlDefinition {
    private final Raml raml;
    private final SchemaValidators schemaValidators;
    private final String baseUri;

    public RamlDefinition(Raml raml, SchemaValidators schemaValidators, String baseUri) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.baseUri = baseUri;
    }

    public Raml getRaml() {
        return raml;
    }

    public RamlDefinition(Raml raml, SchemaValidators schemaValidators) {
        this(raml, schemaValidators, null);
    }

    public RamlDefinition assumingBaseUri(String baseUri) {
        return new RamlDefinition(raml, schemaValidators, baseUri);
    }

    public RamlReport testAgainst(RamlRequest request, RamlResponse response) {
        return createTester().check(request, response);
    }

    public RamlReport testAgainst(MvcResult mvcResult) {
        return matches().testAgainst(mvcResult);
    }

    public RamlReport testAgainst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        return new ServletTester(createTester()).testAgainst(request, response, chain);
    }

    public RamlMatcher matches() {
        return new RamlMatcher(createTester());
    }

    public RamlRestTemplate createRestTemplate(ClientHttpRequestFactory requestFactory) {
        return new RamlRestTemplate(createTester(), requestFactory);
    }

    public RamlRestTemplate createRestTemplate(RestTemplate restTemplate) {
        return new RamlRestTemplate(createTester(), restTemplate);
    }

    public RamlHttpClient createHttpClient() {
        return new RamlHttpClient(createTester());
    }

    public RamlHttpClient createHttpClient(CloseableHttpClient httpClient) {
        return new RamlHttpClient(createTester(), httpClient);
    }

    public RamlChecker createTester() {
        return new RamlChecker(raml, schemaValidators.getValidators(), baseUri);
    }

}

