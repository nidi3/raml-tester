package guru.nidi.ramltester;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class RamlTesterTest extends TestBase {

    @Test
    public void simpleOk() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\""));

        assertTrue(violations.getViolations().isEmpty());
    }

    @Test
    public void undefinedResource() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data2"),
                jsonResponse(200, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Resource /data2 not defined"));
    }

    @Test
    public void undefinedAction() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                post("/data"),
                jsonResponse(200, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Action POST not defined"));
    }

    @Test
    public void undefinedQueryParameter() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data?a=b"),
                jsonResponse(200, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Query parameter 'a' not defined"));
    }

    @Test
    public void illegalyRepeatQueryParameter() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/query?req=1&req=2"),
                jsonResponse(200, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), allOf(startsWith("Query parameter 'req'"), endsWith("is not repeat but found repeatedly in response")));
    }

    @Test
    public void allowedRepeatQueryParameter() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/query?rep=1&rep=2&req=3"),
                jsonResponse(200, "\"hula\""));

        assertTrue(violations.getViolations().isEmpty());
    }

    @Test
    public void missingRequiredQueryParameter() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/query?"),
                jsonResponse(200, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), allOf(startsWith("Query parameter 'req'"), endsWith("is required but not found in response")));
    }

    @Test
    public void undefinedResponseCode() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(201, "\"hula\""));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Response code 201 not defined"));
    }

    @Test
    public void noMediaType() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", null));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Response has no Content-Type header"));
    }

    @Test
    public void undefinedMediaType() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", "text/plain"));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), startsWith("Media type 'text/plain' not defined"));
    }

    @Test
    public void compatibleMediaType() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/data"),
                jsonResponse(200, "\"hula\"", "application/json;charset=utf-8"));

        assertTrue(violations.getViolations().isEmpty());
    }

    @Test
    public void notMatchingSchemaInline() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(200, "5"));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaInclude() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(201, "5"));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), contains("does not match schema"));
    }

    @Test
    public void notMatchingSchemaReferenced() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(202, "5"));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), contains("does not match schema"));
    }

    @Test
    public void undefinedSchema() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/schema"),
                jsonResponse(203, "5"));

        assertEquals(1, violations.getViolations().size());
        assertThat(violations.getViolations().get(0), contains("Schema 'undefined' referenced but not defined"));
    }

    @Test
    public void defaultMediaType() throws Exception {
        final RamlViolations violations = new RamlTester().test(
                raml("simple.raml"),
                get("/mediaType"),
                jsonResponse(200, "\"hula\"","application/default"));

        assertTrue(violations.getViolations().isEmpty());
    }

}
