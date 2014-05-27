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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
public class HeaderTest extends HighlevelTestBase {
    private RamlDefinition simple = RamlLoaders.fromClasspath(getClass()).load("simple.raml");

    @Test
    public void undefinedRequestHeader() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/data").header("a", "b"),
                jsonResponse(200, "\"hula\""),
                equalTo("Header 'a' on action(GET /data) is not defined"));
    }

    @Test
    public void illegallyRepeatRequestHeader() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/header").header("req", "1").header("req", "2"),
                jsonResponse(200, "\"hula\""),
                equalTo("Header 'req' on action(GET /header) is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatRequestHeader() throws Exception {
        assertNoViolations(
                simple,
                get("/header").header("rep", "1").header("rep", "2").header("req", "xxx"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void missingRequiredRequestHeader() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/header"),
                jsonResponse(200, "\"hula\""),
                equalTo("Header 'req' on action(GET /header) is required but not found"));
    }

    @Test
    public void wildcardRequestHeader() throws Exception {
        assertNoViolations(
                simple,
                get("/header").header("x-bla", "1").header("x-hula", "2").header("req", "3"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void missingRequiredWildcardRequestHeader() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/header/reqwild"),
                jsonResponse(200),
                equalTo("Header 'x-{?}' on action(GET /header/reqwild) is required but not found"));
    }

    @Test
    public void existingRequiredWildcardRequestHeader() throws Exception {
        assertNoViolations(
                simple,
                get("/header/reqwild").header("x-", "w"),
                jsonResponse(200));
    }

    @Test
    public void undefinedResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("a", "b");
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                response,
                equalTo("Header 'a' on action(GET /data) is not defined"));
    }

    @Test
    public void illegallyRepeatResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("req", "1");
        response.addHeader("req", "2");
        assertOneResponseViolationThat(
                simple,
                get("/resheader"),
                response,
                equalTo("Header 'req' on action(GET /resheader) is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("rep", "1");
        response.addHeader("rep", "2");
        response.addHeader("req", "xxx");
        assertNoViolations(
                simple,
                get("/resheader"),
                response);
    }

    @Test
    public void missingRequiredResponseHeader() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/resheader"),
                jsonResponse(200, "\"hula\""),
                equalTo("Header 'req' on action(GET /resheader) is required but not found"));
    }

    @Test
    public void wildcardResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("x-bla", "1");
        response.addHeader("x-hula", "2");
        response.addHeader("req", "3");
        assertNoViolations(
                simple,
                get("/resheader"),
                response);
    }

    @Test
    public void missingRequiredWildcardResponseHeader() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/resheader/reqwild"),
                jsonResponse(200),
                equalTo("Header 'x-{?}' on action(GET /resheader/reqwild) is required but not found"));
    }

    @Test
    public void existingRequiredWildcardResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200);
        response.addHeader("x-", "w");
        assertNoViolations(
                simple,
                get("/resheader/reqwild"),
                response);
    }

}
