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
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.UnifiedApi;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import guru.nidi.ramltester.servlet.ServletTester;
import guru.nidi.ramltester.spring.RamlMatcher;
import guru.nidi.ramltester.spring.RamlRestTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.raml.v2.api.RamlModelResult;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

/**
 *
 */
public class RamlDefinition {
    private final CheckerConfig config;

    public RamlDefinition(RamlModelResult raml, SchemaValidators schemaValidators) {
        this(new CheckerConfig(raml, schemaValidators.getValidators()));
    }

    public RamlDefinition(CheckerConfig config) {
        this.config = config;
    }

    public RamlDefinition assumingBaseUri(String baseUri) {
        return new RamlDefinition(config.assumingBaseUri(baseUri));
    }

    public RamlDefinition assumingBaseUri(String baseUri, boolean includeServletPath) {
        return new RamlDefinition(config.assumingBaseUri(baseUri, includeServletPath));
    }

    public RamlDefinition ignoringXheaders() {
        return ignoringXheaders(true);
    }

    public RamlDefinition ignoringXheaders(boolean ignoreXheaders) {
        return new RamlDefinition(config.ignoringXheaders(ignoreXheaders));
    }

    public RamlDefinition includeServletPath() {
        return includeServletPath(true);
    }

    public RamlDefinition includeServletPath(boolean includeServletPath) {
        return new RamlDefinition(config.includeServletPath(includeServletPath));
    }

    public UnifiedApi getRaml() {
        return config.getRaml();
    }

    /**
     * Will throw a {@link RamlViolationException} in case there are errors on the {@link RamlReport}
     *
     * @return {@link RamlDefinition}
     */
    public RamlDefinition failFast() {
        return failFast(true);
    }

    public RamlDefinition failFast(boolean failFast) {
        return new RamlDefinition(config.failFast(failFast));
    }

    public RamlModelResult getModel() {
        return config.raml;
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

    public RestAssuredClient createRestAssured() {
        return new RestAssuredClient(createTester());
    }

    public guru.nidi.ramltester.restassured3.RestAssuredClient createRestAssured3() {
        return new guru.nidi.ramltester.restassured3.RestAssuredClient(createTester());
    }

    public CheckingWebTarget createWebTarget(WebTarget target) {
        return new CheckingWebTarget(createTester(), target);
    }

    public RamlChecker createTester() {
        return new RamlChecker(config);
    }

    public RamlValidator validator() {
        return new RamlValidator(config.getRaml(), config.schemaValidators);
    }

    public RamlReport validate() {
        return validator().validate();
    }
}

