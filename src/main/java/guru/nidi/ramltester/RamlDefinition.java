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

import guru.nidi.ramltester.core.*;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import guru.nidi.ramltester.spring.RamlMatcher;
import guru.nidi.ramltester.spring.RamlRestTemplate;
import org.raml.model.Raml;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class RamlDefinition {
    private final Raml raml;
    private final SchemaValidator schemaValidator;

    public RamlDefinition(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public static RamlLoaders load(String name) {
        return new RamlLoaders(name, null, null);
    }

    public RamlTester createTester() {
        return new RamlTester(raml, schemaValidator);
    }

    public RamlReport testAgainst(RamlRequest request, RamlResponse response) {
        return createTester().test(request, response);
    }

    public RamlReport testAgainst(MvcResult mvcResult, String servletUri) {
        return matches().assumingServletUri(servletUri).testAgainst(mvcResult);
    }

    public RamlMatcher matches() {
        return new RamlMatcher(createTester(), null);
    }

    public RamlRestTemplate createRestTemplate(ClientHttpRequestFactory requestFactory) {
        return new RamlRestTemplate(createTester(), null, requestFactory);
    }

    public RamlRestTemplate createRestTemplate(RestTemplate restTemplate) {
        return new RamlRestTemplate(createTester(), null, restTemplate);
    }

    public RamlReport testAgainst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletRamlRequest httpRequest = new ServletRamlRequest((HttpServletRequest) request);
        final ServletRamlResponse httpResponse = new ServletRamlResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return testAgainst(httpRequest, httpResponse);
    }

}

