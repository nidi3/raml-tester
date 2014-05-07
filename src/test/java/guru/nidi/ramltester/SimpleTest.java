package guru.nidi.ramltester;

import org.junit.Test;

/**
 *
 */
public class SimpleTest extends TestBase {

    private RamlDefinition simple = RamlDefinition.fromClasspath(getClass(), "simple.raml");

    @Test
    public void simpleOk() throws Exception {
        assertNoViolation(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void undefinedResource() throws Exception {
        assertOneViolationThat(
                simple,
                get("/data2"),
                jsonResponse(200, "\"hula\""),
                startsWith("Resource '/data2' not defined"));
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneViolationThat(
                simple,
                post("/data"),
                jsonResponse(200, "\"hula\""),
                startsWith("Action POST not defined"));
    }

    @Test
    public void undefinedQueryParameter() throws Exception {
        assertOneViolationThat(
                simple,
                get("/data?a=b"),
                jsonResponse(200, "\"hula\""),
                endsWith("query parameter 'a' is not defined"));
    }

    @Test
    public void illegallyRepeatQueryParameter() throws Exception {
        assertOneViolationThat(
                simple,
                get("/query?req=1&req=2"),
                jsonResponse(200, "\"hula\""),
                endsWith("query parameter 'req' is not repeat but found repeatedly in response"));
    }

    @Test
    public void allowedRepeatQueryParameter() throws Exception {
        assertNoViolation(
                simple,
                get("/query?rep=1&rep=2&req=3"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void missingRequiredQueryParameter() throws Exception {
        assertOneViolationThat(
                simple,
                get("/query?"),
                jsonResponse(200, "\"hula\""),
                endsWith("query parameter 'req' is required but not found in response"));
    }

    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneViolationThat(
                simple,
                get("/data"),
                jsonResponse(201, "\"hula\""),
                startsWith("Response code 201 not defined"));
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", null),
                startsWith("Response has no Content-Type header"));
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneViolationThat(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "text/plain"),
                startsWith("Media type 'text/plain' not defined"));
    }

    @Test
    public void compatibleMediaType() throws Exception {
        assertNoViolation(
                simple,
                get("/data"),
                jsonResponse(200, "\"hula\"", "application/json;charset=utf-8"));
    }

    @Test
    public void notMatchingSchemaInline() throws Exception {
        assertOneViolationThat(
                simple,
                get("/schema"),
                jsonResponse(200, "5"),
                contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaInclude() throws Exception {
        assertOneViolationThat(
                simple,
                get("/schema"),
                jsonResponse(201, "5"),
                contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaReferenced() throws Exception {
        assertOneViolationThat(
                simple,
                get("/schema"),
                jsonResponse(202, "5"),
                contains("does not match schema"));
    }

    @Test
    public void undefinedSchema() throws Exception {
        assertOneViolationThat(
                simple,
                get("/schema"),
                jsonResponse(203, "5"),
                contains("Schema 'undefined' referenced but not defined"));
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertNoViolation(
                simple,
                get("/mediaType"),
                jsonResponse(200, "\"hula\"", "application/default"));
    }

}
