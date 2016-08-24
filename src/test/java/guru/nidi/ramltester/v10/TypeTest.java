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
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 */
public class TypeTest extends HighlevelTestBase {
    private final RamlDefinition type = RamlLoaders.fromClasspath(getClass()).load("type.raml");

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH); //to ensure . as decimal separator
    }

    @Test
    public void booleanOk() throws Exception {
        for (final String value : new String[]{"true", "false"}) {
            assertNoViolations(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void booleanNok() throws Exception {
        for (final String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'boolean' on action(GET /type) - Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerOk() throws Exception {
        for (final String value : new String[]{"0", "-1", "123456789"}) {
            assertNoViolations(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (final String value : new String[]{"-5", "0", "666"}) {
            assertNoViolations(
                    type,
                    get("/type?integerLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void integerNok() throws Exception {
        for (final String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'integer' on action(GET /type) - Value '" + value + "' is not a valid integer"));
        }
        assertOneRequestViolationThat(
                type,
                get("/type?integerLimit=-6"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'integerLimit' on action(GET /type) - Value '-6' is smaller than minimum -5"));
        assertOneRequestViolationThat(
                type,
                get("/type?integerLimit=667"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'integerLimit' on action(GET /type) - Value '667' is bigger than maximum 666"));
    }

    @Test
    public void numberOk() throws Exception {
        for (final String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolations(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (final String value : new String[]{"5e-2", "0.05", "666.6"}) {
            assertNoViolations(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void numberNok() throws Exception {
        for (final String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'number' on action(GET /type) - Value '" + value + "' is not a valid number"));
        }
        for (final String value : new String[]{"4.9e-2", "0.0049999"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'numberLimit' on action(GET /type) - Value '" + value + "' is smaller than minimum 0.05"));
        }
        for (final String value : new String[]{"666.60001"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'numberLimit' on action(GET /type) - Value '" + value + "' is bigger than maximum 666.6"));
        }
        for (final String value : new String[]{"inf", "-inf", "nan"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?numberLimit=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'numberLimit' on action(GET /type) - Value '" + value + "' is not inside any minimum/maximum"));
        }
    }

    @Test
    public void dateOk() throws Exception {
        for (final String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertNoViolations(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void dateNok() throws Exception {
        for (final String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?date=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'date' on action(GET /type) - Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringOk() throws Exception {
        for (final String value : new String[]{"aa", "12345"}) {
            assertNoViolations(
                    type,
                    get("/type?string=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void stringNok() throws Exception {
        assertOneRequestViolationThat(
                type,
                get("/type?string=a"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'string' on action(GET /type) - Value 'a' is shorter than minimum length 2"));
        assertOneRequestViolationThat(
                type,
                get("/type?string=123456"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'string' on action(GET /type) - Value '123456' is longer than maximum length 5"));
    }

    @Test
    public void enumOk() throws Exception {
        for (final String value : new String[]{"a", "b"}) {
            assertNoViolations(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""));
        }
    }

    @Test
    public void enumNok() throws Exception {
        for (final String value : new String[]{"", "ab", "c"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'enum' on action(GET /type) - Value '" + value + "' is not a member of enum '[a, b]'"));
        }
    }

//    @Test
//    public void multiTypeOk() throws Exception {
//        for (String value : new String[]{"5", "666", "a", "b"}) {
//            assertNoViolation(
//                    type,
//                    get("/type?multi=" + value),
//                    response(200, "\"hula\""));
//        }
//    }
//
//    @Test
//    public void multiTypeNok() throws Exception {
//        for (String value : new String[]{"4", "4.5", "c"}) {
//            assertOneRequestViolationThat(
//                    type,
//                    get("/type?multi=" + value),
//                    response(200, "\"hula\""),
//                    startsWith("Query parameter 'enum' : Value '" + value + "' is not a member of enum '[a, b]'"));
//        }
//    }

    @Test
    public void simplePattern() throws Exception {
        for (final String value : new String[]{"12/a", "00/y"}) {
            assertNoViolations(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (final String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'pattern1' on action(GET /type) - Value '" + value + "' does not match pattern '\\d{2}/[a-y]'"));
        }
    }

    @Test
    public void slashedPattern() throws Exception {
        for (final String value : new String[]{"12/a", "00/y"}) {
            assertNoViolations(
                    type,
                    get("/type?pattern2=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (final String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?pattern2=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'pattern2' on action(GET /type) - Value '" + value + "' does not match pattern '/\\d{2}\\/[a-y]/'"));
        }
    }

    @Test
    public void modifiedPattern() throws Exception {
        assertModifiedPattern("pattern3");
        assertModifiedPattern("pattern4");
        assertModifiedPattern("pattern5");
    }

    private void assertModifiedPattern(String param) throws Exception {
        for (final String value : new String[]{"12/a", "00/y", "99/A"}) {
            assertNoViolations(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""));
        }
        for (final String value : new String[]{"", "12/z", "1/a"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?" + param + "=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter '" + param + "' on action(GET /type) - Value '" + value + "' does not match pattern '/\\d{2}\\/[a-y]/i'"));
        }
    }

    @Test
    public void emptyResponseMediaTypeNotAllowed() throws Exception {
        assertOneResponseViolationThat(
                type,
                post("/empty"),
                response(200, "", null),
                equalTo("No Content-Type header given"));
    }

    @Test
    public void emptyResponseMediaTypeAllowed() throws Exception {
        assertNoViolations(
                type,
                post("/empty"),
                response(201, "", null));
        assertNoViolations(
                type,
                post("/empty"),
                response(202, "", "a/b"));
    }

    @Test
    public void responseBodyNotAllowed() throws Exception {
        assertOneResponseViolationThat(
                type,
                post("/empty"),
                jsonResponse(201, "\"hula\""),
                equalTo("Body given but none defined on action(POST /empty) response(201)"));
    }
}
