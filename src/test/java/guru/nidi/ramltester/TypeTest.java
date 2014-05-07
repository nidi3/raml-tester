package guru.nidi.ramltester;

import org.junit.Test;

/**
 *
 */
public class TypeTest extends TestBase {
    private RamlDefinition type = RamlDefinition.fromClasspath(getClass(), "type.raml");

    @Test
    public void booleanOk() throws Exception {
        for (String value : new String[]{"true", "false"}) {
            assertNoViolation(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void booleanNok() throws Exception {
        for (String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneViolationThat(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'boolean': Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerOk() throws Exception {
        for (String value : new String[]{"0", "-1", "123456789"}) {
            assertNoViolation(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"-5", "0", "666"}) {
            assertNoViolation(
                    type,
                    get("/type?integerLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void integerNok() throws Exception {
        for (String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x"}) {
            assertOneViolationThat(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'integer': Value '" + value + "' is not a valid integer"));
        }
        for (String value : new String[]{"-6", "667"}) {
            assertOneViolationThat(
                    type,
                    get("/type?integerLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    contains("query parameter 'integerLimit': Value '" + value + "' is "));
        }
    }

    @Test
    public void numberOk() throws Exception {
        for (String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolation(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"5e-2", "0.05", "666.6"}) {
            assertNoViolation(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void numberNok() throws Exception {
        for (String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneViolationThat(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'number': Value '" + value + "' is not a valid number"));
        }
        for (String value : new String[]{"4.9e-2", "0.0049999", "666.60001", "inf", "-inf", "nan"}) {
            assertOneViolationThat(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    contains("query parameter 'numberLimit': Value '" + value + "' is "));
        }
    }

    @Test
    public void dateOk() throws Exception {
        for (String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertNoViolation(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void dateNok() throws Exception {
        for (String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT"}) {
            assertOneViolationThat(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'date': Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringOk() throws Exception {
        for (String value : new String[]{"aa", "12345"}) {
            assertNoViolation(
                    type,
                    get("/type?string=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void stringNok() throws Exception {
        for (String value : new String[]{"", "a", "123456"}) {
            assertOneViolationThat(
                    type,
                    get("/type?string=" + value),
                    jsonResponse(200, "\"hula\""),
                    contains("query parameter 'string': Value '" + value + "' is "));
        }
    }

    @Test
    public void enumOk() throws Exception {
        for (String value : new String[]{"a", "b"}) {
            assertNoViolation(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void enumNok() throws Exception {
        for (String value : new String[]{"", "ab", "c"}) {
            assertOneViolationThat(
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
//            assertOneViolationThat(
//                    type,
//                    get("/type?multi=" + value),
//                    jsonResponse(200, "\"hula\""),
//                    startsWith("Query parameter 'enum' : Value '" + value + "' is not a member of enum '[a, b]'"));
//        }
//    }

    @Test
    public void simplePattern() throws Exception {
        for (String value : new String[]{"12/a", "00/y"}) {
            assertNoViolation(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneViolationThat(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter 'pattern1': Value '" + value + "' does not match pattern '\\d{2}/[a-y]'"));
        }
    }

    @Test
    public void slashedPattern() throws Exception {
        for (String value : new String[]{"12/a", "00/y"}) {
            assertNoViolation(
                    type,
                    get("/type?pattern2=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneViolationThat(
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
            assertNoViolation(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (String value : new String[]{"", "12/z", "1/a"}) {
            assertOneViolationThat(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""),
                    endsWith("query parameter '" + param + "': Value '" + value + "' does not match pattern '/\\d{2}\\/[a-y]/i'"));
        }
    }


}
