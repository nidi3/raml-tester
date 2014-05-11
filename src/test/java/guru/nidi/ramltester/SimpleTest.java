package guru.nidi.ramltester;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 *
 */
public class SimpleTest extends HighlevelTestBase {

    private RamlDefinition simple = TestRaml.load("simple.raml").fromClasspath(getClass());

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
    public void notMatchingSchemaInline() throws Exception {
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
    public void notMatchingSchemaInclude() throws Exception {
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
    public void notMatchingSchemaReferenced() throws Exception {
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
    public void undefinedSchema() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(203, "5"),
                equalTo("Schema 'undefined' is referenced but not defined on action(GET /schema) response(203) mime-type('application/json')"));
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertNoViolations(
                simple,
                get("/mediaType"),
                jsonResponse(200, "\"hula\"", "application/default"));
    }

}
