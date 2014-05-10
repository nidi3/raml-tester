package guru.nidi.ramltester;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

/**
 *
 */
public class TypeTest extends HighlevelTestBase {
    private RamlDefinition type = TestRaml.load("type.raml").fromClasspath(getClass());

    @Test
    public void booleanOk() throws Exception {
        for (String value : new String[]{"true", "false"}) {
            assertNoViolations(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void booleanNok() throws Exception {
        for (String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'boolean': Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerOk() throws Exception {
        for (String value : new String[]{"0", "-1", "123456789"}) {
            assertNoViolations(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"-5", "0", "666"}) {
            assertNoViolations(
                    type,
                    get("/type?integerLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void integerNok() throws Exception {
        for (String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'integer': Value '" + value + "' is not a valid integer"));
        }
        for (String value : new String[]{"-6", "667"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?integerLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    containsString("query parameter 'integerLimit': Value '" + value + "' is "));
        }
    }

    @Test
    public void numberOk() throws Exception {
        for (String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolations(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"5e-2", "0.05", "666.6"}) {
            assertNoViolations(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void numberNok() throws Exception {
        for (String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'number': Value '" + value + "' is not a valid number"));
        }
        for (String value : new String[]{"4.9e-2", "0.0049999", "666.60001", "inf", "-inf", "nan"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    containsString("query parameter 'numberLimit': Value '" + value + "' is "));
        }
    }

    @Test
    public void dateOk() throws Exception {
        for (String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertNoViolations(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void dateNok() throws Exception {
        for (String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'date': Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringOk() throws Exception {
        for (String value : new String[]{"aa", "12345"}) {
            assertNoViolations(
                    type,
                    get("/type?string=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void stringNok() throws Exception {
        for (String value : new String[]{"", "a", "123456"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?string=" + value),
                    jsonResponse(200, "\"hula\""),
                    containsString("query parameter 'string': Value '" + value + "' is "));
        }
    }

    @Test
    public void enumOk() throws Exception {
        for (String value : new String[]{"a", "b"}) {
            assertNoViolations(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void enumNok() throws Exception {
        for (String value : new String[]{"", "ab", "c"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'enum': Value '" + value + "' is not a member of enum '[a, b]'"));
        }
    }

//    @Test
//    public void multiTypeOk() throws Exception {
//        for (String value : new String[]{"5", "666", "a", "b"}) {
//            assertNoViolation(
//                    type,
//                    get("/type?multi=" + value),
//                    jsonResponse(200, "\"hula\""));
//        }
//    }
//
//    @Test
//    public void multiTypeNok() throws Exception {
//        for (String value : new String[]{"4", "4.5", "c"}) {
//            assertOneRequestViolationThat(
//                    type,
//                    get("/type?multi=" + value),
//                    jsonResponse(200, "\"hula\""),
//                    startsWith("Query parameter 'enum' : Value '" + value + "' is not a member of enum '[a, b]'"));
//        }
//    }

    @Test
    public void simplePattern() throws Exception {
        for (String value : new String[]{"12/a", "00/y"}) {
            assertNoViolations(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'pattern1': Value '" + value + "' does not match pattern '\\d{2}/[a-y]'"));
        }
    }

    @Test
    public void slashedPattern() throws Exception {
        for (String value : new String[]{"12/a", "00/y"}) {
            assertNoViolations(
                    type,
                    get("/type?pattern2=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?pattern2=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'pattern2': Value '" + value + "' does not match pattern '/\\d{2}\\/[a-y]/'"));
        }
    }

    @Test
    public void modifiedPattern() throws Exception {
        assertModifiedPattern("pattern3");
        assertModifiedPattern("pattern4");
        assertModifiedPattern("pattern5");
    }

    private void assertModifiedPattern(String param) throws Exception {
        for (String value : new String[]{"12/a", "00/y", "99/A"}) {
            assertNoViolations(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter '" + param + "': Value '" + value + "' does not match pattern '/\\d{2}\\/[a-y]/i'"));
        }
    }

    @Test
    public void emptyResponseMediaTypeNotAllowed() throws Exception {
        assertOneResponseViolationThat(
                type,
                post("/empty"),
                jsonResponse(200, "", null),
                startsWith("Response has no Content-Type header"));
    }

    @Test
    public void emptyResponseMediaTypeAllowed() throws Exception {
        assertNoViolations(
                type,
                post("/empty"),
                jsonResponse(201, "", null));
        assertNoViolations(
                type,
                post("/empty"),
                jsonResponse(202, "", "a/b"));
    }

    @Test
    public void responseBodyNotAllowed() throws Exception {
        assertOneResponseViolationThat(
                type,
                post("/empty"),
                jsonResponse(201, "\"hula\""),
                startsWith("Response body given but none defined on action"));
    }
}
