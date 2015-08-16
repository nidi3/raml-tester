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

import guru.nidi.ramltester.junit.ExpectedUsage;
import guru.nidi.ramltester.spring.SpringMockRamlRequest;
import guru.nidi.ramltester.spring.SpringMockRamlResponse;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;

import static guru.nidi.ramltester.core.UsageItem.RESOURCE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 */
public class SimpleTest extends HighlevelTestBase {

    private static RamlDefinition simple = RamlLoaders.fromClasspath(SimpleTest.class).load("simple.raml");
    private static RamlDefinition noBaseUri = RamlLoaders.fromClasspath(SimpleTest.class).load("noBaseUri.raml");
    private static MultiReportAggregator aggregator = new MultiReportAggregator();

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator.usageProvider(simple), RESOURCE);

    @Test
    public void simpleOk() throws Exception {
        assertNoViolations(test(aggregator, simple, get("/"), jsonResponse(200)));
        assertNoViolations(test(aggregator, simple, get("/d"), jsonResponse(200)));
        assertNoViolations(test(aggregator, simple, get("/data"), jsonResponse(200, "\"hula\"")));
    }

    @Test
    public void undefinedResource() throws Exception {
        assertOneRequestViolationThat(
                test(aggregator, simple, get("/data2"), jsonResponse(200, "\"hula\"")),
                equalTo("Resource '/data2' is not defined")
        );
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneRequestViolationThat(
                test(aggregator, simple, post("/data"), jsonResponse(200, "\"hula\"")),
                equalTo("Action POST is not defined on resource(/data)")
        );
    }


    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneResponseViolationThat(
                test(aggregator, simple, get("/data"), jsonResponse(201, "\"hula\"")),
                equalTo("Response(201) is not defined on action(GET /data)")
        );
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneResponseViolationThat(
                test(aggregator, simple, get("/data"), response(200, "\"hula\"", null)),
                equalTo("No Content-Type header given")
        );
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/data"),
                        response(200, "\"hula\"", "text/plain")),
                equalTo("Media type 'text/plain' is not defined on action(GET /data) response(200)")
        );
    }

    @Test
    public void emptyResponseBody() throws Exception {
        assertOneResponseViolationThat(
                test(aggregator, simple, get("/data"), jsonResponse(200)),
                equalTo("Schema defined but empty body for media type 'application/json' on action(GET /data) response(200) mime-type('abc/xyz+json')")
        );
    }

    @Test
    public void compatibleMediaType() throws Exception {
        assertNoViolations(
                test(aggregator, simple, get("/data"), response(200, "\"hula\"", "application/json;charset=utf-8")));
    }

    @Test
    public void undefinedSchema() throws Exception {
        assertOneResponseViolationThat(
                test(aggregator, simple, get("/schema"), jsonResponse(203, "5")),
                startsWith("Body does not match schema for action(GET /schema) response(203) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: Schema invalid:")
        );
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        RamlLoaders.fromClasspath(getClass()).addSchemaValidator(new DefaultOkSchemaValidator()).load("simple.raml"),
                        get("/mediaType"),
                        response(200, "\"hula\"", "application/default")),
                equalTo("Body does not match schema for action(GET /mediaType) response(200) mime-type('application/default')\n" +
                        "Content: \"hula\"\n" +
                        "Message: ok")
        );
    }

    @Test
    public void ambiguousMediaTypesInRequest() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                        simple,
                        post("/mediaType").content("\"hula\"").contentType(MediaType.APPLICATION_JSON),
                        jsonResponse(201)),
                equalTo("Ambiguous definition: mime-type('application/json') and also mime-type('abc/xyz+json') used on action(POST /mediaType)"));
    }

    @Test
    public void ambiguousMediaTypesInResponse() throws Exception {
        assertOneResponseViolationThat(
                test(aggregator, simple, get("/mediaType"), jsonResponse(201, "\"hula\"")),
                equalTo("Ambiguous definition: mime-type('application/json') and also mime-type('abc/xyz+json') used on action(GET /mediaType) response(201)"));
    }

    @Test
    public void acceptNoBaseUri() throws Exception {
        assertNoViolations(noBaseUri.testAgainst(
                new SpringMockRamlRequest(get("/base/path").buildRequest(new MockServletContext())),
                new SpringMockRamlResponse(jsonResponse(200))));
    }

    @Test
    public void acceptNoBaseUriAssumingBaseUri() throws Exception {
        assertNoViolations(noBaseUri.assumingBaseUri("http://server/base").testAgainst(
                new SpringMockRamlRequest(get("/path").buildRequest(new MockServletContext())),
                new SpringMockRamlResponse(jsonResponse(200))));
    }
}
