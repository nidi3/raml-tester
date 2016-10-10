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

import guru.nidi.ramltester.violations.JsonSchemaRamlViolationCause;
import guru.nidi.ramltester.violations.XmlRamlViolationCause;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
public class SchemaTest extends HighlevelTestBase {
    private final RamlDefinition simple = RamlLoaders.fromClasspath(getClass()).load("simple.raml");

    @Test
    public void matchingJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(200, "\"str\""));
    }

    @Test
    public void matchingXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), response(208, "<api-request><input>str</input></api-request>", "text/xml"));
    }

    @Test
    public void matchingReferencedJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(204, "\"str\""));
        assertNoViolations(simple, get("/schema"), jsonResponse(205, "\"str\""));
    }

    @Test
    public void matchingReferencedXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), response(206, "<api-request><input>str</input></api-request>", "application/xml"));
        assertNoViolations(simple, get("/schema"), response(207, "<api-request><input>str</input></api-request>", "application/xml"));
    }

    @Test
    public void notMatchingJsonSchemaInline() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(200, "5"),
                startsWith("Body does not match schema for action(GET /schema) response(200) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: "),
                instanceOf(JsonSchemaRamlViolationCause.class)
        );
    }

    @Test
    public void notMatchingJsonSchemaInclude() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(201, "5"),
                startsWith("Body does not match schema for action(GET /schema) response(201) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: "),
                instanceOf(JsonSchemaRamlViolationCause.class)
        );
    }

    @Test
    public void notMatchingJsonSchemaReferenced() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(202, "5"),
                startsWith("Body does not match schema for action(GET /schema) response(202) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: "),
                instanceOf(JsonSchemaRamlViolationCause.class)
        );
    }

    @Test
    public void notMatchingXmlSchemaInline() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                response(208, "<api-request>str</api-request>", "text/xml"),
                startsWith("Body does not match schema for action(GET /schema) response(208) mime-type('text/xml')\n" +
                        "Content: <api-request>str</api-request>\n" +
                        "Message: "),
                instanceOf(XmlRamlViolationCause.class)
        );
    }

    @Test
    public void notMatchingXmlSchemaInclude() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                response(206, "5", "application/xml"),
                startsWith("Body does not match schema for action(GET /schema) response(206) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Message: "),
                instanceOf(XmlRamlViolationCause.class)
        );
    }

    @Test
    public void notMatchingXmlSchemaReferenced() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                response(207, "5", "application/xml"),
                startsWith("Body does not match schema for action(GET /schema) response(207) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Message: "),
                instanceOf(XmlRamlViolationCause.class)
        );
    }

    @Test
    public void noSchemaValidatorForMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                response(209, "5", "text/bla"),
                equalTo("No SchemaValidator found for media type 'text/bla' on action(GET /schema) response(209) mime-type('text/bla')")
        );
    }

    @Test
    public void invalidSchema() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(210, "5"),
                equalTo("Body does not match schema for action(GET /schema) response(210) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Message: Schema invalid: Unrecognized token 'xxx': was expecting ('true', 'false' or 'null')\n" +
                        " at [Source: Schema 'invalid'; line: 1, column: 7]")
        );
    }

}
