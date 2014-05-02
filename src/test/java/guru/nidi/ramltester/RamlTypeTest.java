package guru.nidi.ramltester;

import org.junit.Test;

/**
 *
 */
public class RamlTypeTest extends TestBase {

    @Test
    public void booleanOk() throws Exception {
        for (String value : new String[]{"true", "false"}) {
            assertNoViolation(
                    raml("simple.raml"),
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void booleanNok() throws Exception {
        for (String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneViolationThat(
                    raml("simple.raml"),
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""),
                    startsWith("Query parameter 'boolean' : Value '" + value + "' is not a valid boolean"));
        }
    }
    
    @Test
    public void integerOk() throws Exception {
        for (String value : new String[]{"0", "-1","123456789"}) {
            assertNoViolation(
                    raml("simple.raml"),
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void integerNok() throws Exception {
        for (String value : new String[]{"", "-0", "+1", "1.", "1.0","123456x"}) {
            assertOneViolationThat(
                    raml("simple.raml"),
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""),
                    startsWith("Query parameter 'integer' : Value '" + value + "' is not a valid integer"));
        }
    }

    @Test
    public void numberOk() throws Exception {
        for (String value : new String[]{"0", "inf","-inf","nan","-1","1e-1","1e+1","1.2345e-1123"}) {
            assertNoViolation(
                    raml("simple.raml"),
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void numberNok() throws Exception {
        for (String value : new String[]{"", "-0", "-.1","1.", "1e1","1.123w"}) {
            assertOneViolationThat(
                    raml("simple.raml"),
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""),
                    startsWith("Query parameter 'number' : Value '" + value + "' is not a valid number"));
        }
    }

}
