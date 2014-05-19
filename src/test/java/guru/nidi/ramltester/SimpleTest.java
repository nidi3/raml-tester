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

import guru.nidi.ramltester.core.MediaType;
import guru.nidi.ramltester.core.Message;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.RamlLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static guru.nidi.ramltester.util.TestUtils.getEnv;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 *
 */
public class SimpleTest extends HighlevelTestBase {

    private RamlDefinition simple = RamlTester.fromClasspath(getClass()).load("simple.raml");

    @Test
    public void simpleOk() throws Exception {
        assertNoViolations(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void undefinedResource() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/data2"),
                jsonResponse(200, "\"hula\""),
                equalTo("Resource '/data2' is not defined"));
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneRequestViolationThat(
                simple,
                post("/data"),
                jsonResponse(200, "\"hula\""),
                equalTo("Action POST is not defined on resource(/data)"));
    }

    @Test
    public void undefinedQueryParameter() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/data?a=b"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'a' on action(GET /data) is not defined"));
    }

    @Test
    public void illegallyRepeatQueryParameter() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/query?req=1&req=2"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'req' on action(GET /query) is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatQueryParameter() throws Exception {
        assertNoViolations(
                simple,
                get("/query?rep=1&rep=2&req=3"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void missingRequiredQueryParameter() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/query?"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'req' on action(GET /query) is required but not found"));
    }

    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(201, "\"hula\""),
                equalTo("Response(201) is not defined on action(GET /data)"));
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", null),
                equalTo("Response has no Content-Type header"));
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "text/plain"),
                equalTo("Media type 'text/plain' is not defined on action(GET /data) response(200)"));
    }

    @Test
    public void compatibleMediaType() throws Exception {
        assertNoViolations(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "application/json;charset=utf-8"));
    }

    @Test
    public void matchingJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(200, "\"str\""));
    }

    @Test
    public void matchingXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(208, "<api-request><input>str</input></api-request>", "text/xml"));
    }

    @Test
    public void matchingReferencedJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(204, "\"str\""));
        assertNoViolations(simple, get("/schema"), jsonResponse(205, "\"str\""));
    }

    @Test
    public void matchingReferencedXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(206, "<api-request><input>str</input></api-request>", "application/xml"));
        assertNoViolations(simple, get("/schema"), jsonResponse(207, "<api-request><input>str</input></api-request>", "application/xml"));
    }

    @Test
    public void notMatchingJsonSchemaInline() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(200, "5"),
                startsWith("Response content does not match schema for action(GET /schema) response(200) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: ")
        );
    }

    @Test
    public void notMatchingJsonSchemaInclude() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(201, "5"),
                startsWith("Response content does not match schema for action(GET /schema) response(201) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: ")
        );
    }

    @Test
    public void notMatchingJsonSchemaReferenced() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(202, "5"),
                startsWith("Response content does not match schema for action(GET /schema) response(202) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: ")
        );
    }

    @Test
    public void notMatchingXmlSchemaInline() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                jsonResponse(208, "<api-request>str</api-request>", "text/xml"),
                startsWith("Response content does not match schema for action(GET /schema) response(208) mime-type('text/xml')\n" +
                        "Content: <api-request>str</api-request>\n" +
                        "Message: ")
        );
    }

    @Test
    public void notMatchingXmlSchemaInclude() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                jsonResponse(206, "5", "application/xml"),
                startsWith("Response content does not match schema for action(GET /schema) response(206) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Message: ")
        );
    }

    @Test
    public void notMatchingXmlSchemaReferenced() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                jsonResponse(207, "5", "application/xml"),
                startsWith("Response content does not match schema for action(GET /schema) response(207) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Message: ")
        );
    }

    @Test
    public void noSchemaValidatorForMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(209, "5", "text/bla"),
                equalTo("No SchemaValidator found for media type 'text/bla' on action(GET /schema) response(209)")
        );
    }

    @Test
    public void apiPortalReferenced() throws IOException {
        final RamlLoaders ramlLoader = RamlTester.fromApiPortal(getEnv("API_PORTAL_USER"), getEnv("API_PORTAL_PASS"));
        final RamlDefinition ramlDefinition = ramlLoader.load("test.raml");
        assertNoViolations(ramlDefinition, get("/test"), jsonResponse(200, "\"hula\""));
    }


    @Test
    public void undefinedSchema() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(203, "5"),
                startsWith("Response content does not match schema for action(GET /schema) response(203) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: Schema invalid:")
        );
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertOneResponseViolationThat(
                RamlTester.fromClasspath(getClass()).addSchemaValidator(new DummySchemaValidator()).load("simple.raml"),
                get("/mediaType"),
                jsonResponse(200, "\"hula\"", "application/default"),
                equalTo("Response content does not match schema for action(GET /mediaType) response(200) mime-type('application/default')\n" +
                        "Content: \"hula\"\n" +
                        "Message: ok")
        );
    }

    private static class DummySchemaValidator implements SchemaValidator {
        @Override
        public boolean supports(MediaType mediaType) {
            return mediaType.isCompatibleWith(MediaType.valueOf("application/default"));
        }

        @Override
        public SchemaValidator withResourceLoader(RamlLoader resourceLoader) {
            return this;
        }

        @Override
        public void validate(String content, String schema, RamlViolations violations, Message message) {
            violations.add(message.withParam("ok"));
        }
    }
}
