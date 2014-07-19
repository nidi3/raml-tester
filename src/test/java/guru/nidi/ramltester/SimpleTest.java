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
import guru.nidi.ramltester.loader.RamlLoader;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static guru.nidi.ramltester.core.UsageItem.RESOURCE;
import static guru.nidi.ramltester.util.TestUtils.getEnv;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 */
public class SimpleTest extends HighlevelTestBase {

    private static RamlDefinition simple = RamlLoaders.fromClasspath(SimpleTest.class).load("simple.raml");
    private static MultiReportAggregator aggregator = new MultiReportAggregator();

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator.usageProvider(simple), RESOURCE);

    @Test
    public void simpleOk() throws Exception {
        assertNoViolations(test(aggregator,
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"")));
    }

    @Test
    public void undefinedResource() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                        simple,
                        get("/data2"),
                        jsonResponse(200, "\"hula\"")),
                equalTo("Resource '/data2' is not defined")
        );
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                        simple,
                        post("/data"),
                        jsonResponse(200, "\"hula\"")),
                equalTo("Action POST is not defined on resource(/data)")
        );
    }


    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/data"),
                        jsonResponse(201, "\"hula\"")),
                equalTo("Response(201) is not defined on action(GET /data)")
        );
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/data"),
                        jsonResponse(200, "\"hula\"", null)),
                equalTo("No Content-Type header given")
        );
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/data"),
                        jsonResponse(200, "\"hula\"", "text/plain")),
                equalTo("Media type 'text/plain' is not defined on action(GET /data) response(200)")
        );
    }

    @Test
    public void emptyResponseBody() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/data"),
                        jsonResponse(200)),
                equalTo("Schema defined but empty body for media type 'application/json' on action(GET /data) response(200)")
        );
    }

    @Test
    public void compatibleMediaType() throws Exception {
        assertNoViolations(test(aggregator,
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "application/json;charset=utf-8")));
    }

    @Test
    public void apiPortalReferenced() throws IOException {
        final RamlLoaders ramlLoader = RamlLoaders.fromApiPortal(getEnv("API_PORTAL_USER"), getEnv("API_PORTAL_PASS"));
        final RamlDefinition ramlDefinition = ramlLoader.load("test.raml");
        assertNoViolations(ramlDefinition, get("/test"), jsonResponse(200, "\"hula\""));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void loadFileWithUnfindableReference() {
        RamlLoaders.fromFile(new File("src/test/resources/guru/nidi/ramltester/sub")).load("simple.raml");
    }

    @Test
    public void loadFileWithSecondLoader() {
        RamlLoaders.fromFile(new File("src/test/resources/guru/nidi/ramltester/sub"))
                .andFromClasspath("guru/nidi/ramltester")
                .load("simple.raml");
    }

    @Test
    public void undefinedSchema() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                        simple,
                        get("/schema"),
                        jsonResponse(203, "5")),
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
                        jsonResponse(200, "\"hula\"", "application/default")),
                equalTo("Body does not match schema for action(GET /mediaType) response(200) mime-type('application/default')\n" +
                        "Content: \"hula\"\n" +
                        "Message: ok")
        );
    }


}
