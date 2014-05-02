package guru.nidi.ramltester;

import org.junit.Test;

/**
 *
 */
public class RamlTesterTest extends TestBase {

    @Test
    public void simpleOk() throws Exception {
        assertNoViolation(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void undefinedResource() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/data2"),
                jsonResponse(200, "\"hula\""),
                startsWith("Resource /data2 not defined"));
    }

    @Test
    public void undefinedAction() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                post("/data"),
                jsonResponse(200, "\"hula\""),
                startsWith("Action POST not defined"));
    }

    @Test
    public void undefinedQueryParameter() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/data?a=b"),
                jsonResponse(200, "\"hula\""),
                startsWith("Query parameter 'a' not defined"));
    }

    @Test
    public void illegalyRepeatQueryParameter() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/query?req=1&req=2"),
                jsonResponse(200, "\"hula\""),
                allOf(startsWith("Query parameter 'req'"), endsWith("is not repeat but found repeatedly in response")));
    }

    @Test
    public void allowedRepeatQueryParameter() throws Exception {
        assertNoViolation(
                raml("simple.raml"),
                get("/query?rep=1&rep=2&req=3"),
                jsonResponse(200, "\"hula\""));
    }

    @Test
    public void missingRequiredQueryParameter() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/query?"),
                jsonResponse(200, "\"hula\""),
                allOf(startsWith("Query parameter 'req'"), endsWith("is required but not found in response")));
    }

    @Test
    public void undefinedResponseCode() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(201, "\"hula\""),
                startsWith("Response code 201 not defined"));
    }

    @Test
    public void noMediaType() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", null),
                startsWith("Response has no Content-Type header"));
    }

    @Test
    public void undefinedMediaType() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", "text/plain"),
                startsWith("Media type 'text/plain' not defined"));
    }

    @Test
    public void compatibleMediaType() throws Exception {
        assertNoViolation(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", "application/json;charset=utf-8"));
    }

    @Test
    public void notMatchingSchemaInline() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(200, "5"),
                contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaInclude() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(201, "5"),
                contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaReferenced() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(202, "5"),
                contains("does not match schema"));
    }

    @Test
    public void undefinedSchema() throws Exception {
        assertOneViolationThat(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(203, "5"),
                contains("Schema 'undefined' referenced but not defined"));
    }

    @Test
    public void defaultMediaType() throws Exception {
        assertNoViolation(
                raml("simple.raml"),
                get("/mediaType"),
                jsonResponse(200, "\"hula\"", "application/default"));
    }

}
