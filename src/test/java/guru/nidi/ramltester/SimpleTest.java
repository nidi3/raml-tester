package guru.nidi.ramltester;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

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
                startsWith("Resource '/data2' not defined"));
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneRequestViolationThat(
                simple,
                post("/data"),
                jsonResponse(200, "\"hula\""),
                startsWith("Action POST not defined"));
    }

    @Test
    public void undefinedQueryParameter() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/data?a=b"),
                jsonResponse(200, "\"hula\""),
                endsWith("query parameter 'a' is not defined"));
    }

    @Test
    public void illegallyRepeatQueryParameter() throws Exception {
        assertOneRequestViolationThat(
                simple,
                get("/query?req=1&req=2"),
                jsonResponse(200, "\"hula\""),
                endsWith("query parameter 'req' is not repeat but found repeatedly in response"));
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
                endsWith("query parameter 'req' is required but not found in response"));
    }

    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(201, "\"hula\""),
                startsWith("Response code 201 not defined"));
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", null),
                startsWith("Response has no Content-Type header"));
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "text/plain"),
                startsWith("Media type 'text/plain' not defined"));
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
                containsString("does not match schema"));
    }

    @Test
    public void notMatchingSchemaInclude() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(201, "5"),
                containsString("does not match schema"));
    }

    @Test
    public void notMatchingSchemaReferenced() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(202, "5"),
                containsString("does not match schema"));
    }

    @Test
    public void undefinedSchema() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(203, "5"),
                containsString("Schema 'undefined' referenced but not defined"));
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertNoViolations(
                simple,
                get("/mediaType"),
                jsonResponse(200, "\"hula\"", "application/default"));
    }

}
