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

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.spring.SpringMockRamlRequest;
import guru.nidi.ramltester.spring.SpringMockRamlResponse;
import org.hamcrest.Matcher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HighlevelTestBase {
    protected MockHttpServletRequest get(String url) {
        return MockMvcRequestBuilders.get(url).buildRequest(new MockServletContext());
    }

    protected MockHttpServletRequest post(String url) {
        return MockMvcRequestBuilders.post(url).buildRequest(new MockServletContext());
    }


    protected MockHttpServletResponse jsonResponse(int code, String json, String contentType) throws UnsupportedEncodingException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(code);
        response.setContentType(contentType);
        response.getWriter().print(json);
        return response;
    }

    protected MockHttpServletResponse jsonResponse(int code, String json) throws UnsupportedEncodingException {
        return jsonResponse(code, json, "application/json");
    }

    protected MockHttpServletResponse jsonResponse(int code) throws UnsupportedEncodingException {
        return jsonResponse(code, "", "application/json");
    }

    protected void assertNoViolations(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        final RamlReport report = test(raml, request, response);
        assertNoViolations(report);
    }

    protected void assertNoViolations(RamlReport report) {
        assertTrue("Expected no violations, but found: " + report, report.isEmpty());
    }

    protected void assertNoViolations(RamlViolations violations) {
        assertTrue("Expected no violations, but found: " + violations, violations.isEmpty());
    }

    protected void assertOneRequestViolationThat(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response, Matcher<String> matcher) {
        final RamlReport report = test(raml, request, response);
        assertNoViolations(report.getResponseViolations());
        assertOneViolationThat(report.getRequestViolations(), matcher);
    }

    protected void assertOneResponseViolationThat(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response, Matcher<String> matcher) {
        final RamlReport report = test(raml, request, response);
        assertNoViolations(report.getRequestViolations());
        assertOneViolationThat(report.getResponseViolations(), matcher);
    }

    protected void assertResponseViolationsThat(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response, Matcher<String> matcher) {
        final RamlReport report = test(raml, request, response);
        assertNoViolations(report.getRequestViolations());
        assertViolationsThat(report.getResponseViolations(), matcher);
    }

    private RamlReport test(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        return raml.assumingServletUri("http://nidi.guru/raml/v1").testAgainst(
                new SpringMockRamlRequest(request),
                new SpringMockRamlResponse(response));
    }

    protected void assertOneViolationThat(RamlViolations violations, Matcher<String> matcher) {
        assertThat("Expected exactly one violation", 1, equalTo(violations.size()));
        assertThat(violations.iterator().next(), matcher);
    }

    protected void assertViolationsThat(RamlViolations violations, Matcher<String> matcher) {
        for (String violation : violations) {
            assertThat(violation, matcher);
        }
    }


}
