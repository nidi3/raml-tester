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

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TypeTest extends HighlevelTestBase {
    private final RamlDefinition type = RamlLoaders.fromClasspath(getClass()).load("type.raml");

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH); //to ensure . as decimal separator
    }

    @Test
    public void booleanOk() throws Exception {
        for (final String value : new String[]{"true", "false"}) {
            assertThat(test(type, get("/type?boolean=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    @Ignore("https://github.com/raml-org/raml-java-parser/issues/323")
    public void booleanNok() throws Exception {
        for (final String value : new String[]{"TRUE", "T", "yes", "0", "bla"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?boolean=" + value),
                    jsonResponse(200, "\"hula\""),
                    containsInOrder("Query parameter 'boolean' on action(GET /type) - Value '" + value + "': Invalid type ", "expected Boolean"));
        }
    }

    @Test
    public void integerOk() throws Exception {
        for (final String value : new String[]{"-0", "0", "+1", "-1", "123456789"}) {
            assertThat(test(type, get("/type?integer=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
        for (final String value : new String[]{"-5", "0", "666"}) {
            assertThat(test(type, get("/type?integerLimit=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void integerNok() throws Exception {
        for (final String value : new String[]{"1.", "1.0", "123456x", "w"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?integer=" + value),
                    jsonResponse(200, "\"hula\""),
                    containsInOrder("Query parameter 'integer' on action(GET /type) - Value '" + value + "': Invalid type", "expected Integer"));
        }
        assertOneRequestViolationThat(
                type,
                get("/type?integerLimit=-6"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'integerLimit' on action(GET /type) - Value '-6': Expected number between -5 and 666"));
        assertOneRequestViolationThat(
                type,
                get("/type?integerLimit=667"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'integerLimit' on action(GET /type) - Value '667': Expected number between -5 and 666"));
    }

    @Test
    @Ignore("https://github.com/raml-org/raml-java-parser/issues/322")
    public void numberOk() throws Exception {
        for (final String value : new String[]{"-0", "0", "-1", "-.1", "1.", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertThat(test(type, get("/type?number=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
        for (final String value : new String[]{"5e-2", "0.05", "666.5"}) {
            assertThat(test(type, get("/type?numberLimit=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    @Ignore("https://github.com/raml-org/raml-java-parser/issues/322")
    public void numberNok() throws Exception {
        for (final String value : new String[]{"a", "1.123w"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?number=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'number' on action(GET /type) - Value '" + value + "': Invalid type String, expected Float"));
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
    }

    @Test
    public void dateOnlyOk() throws Exception {
        for (final String value : new String[]{"2016-02-03", "16-02-03"}) {
            assertThat(test(type, get("/type?date-only=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void dateOnlyNok() throws Exception {
        for (final String value : new String[]{"2016-02", "2016-02-03T", "16-02-03T12:34"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?date-only=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'date-only' on action(GET /type) - Value '" + value + "': Provided value " + value + " is not compliant with the format date_only provided in rfc3339"));
        }
    }

    @Test
    @Ignore("https://github.com/raml-org/raml-java-parser/issues/324")
    public void timeOnlyOk() throws Exception {
        for (final String value : new String[]{"01:02:03", "01:02:03.4", "01:02:03.45", "01:02:03.456"}) {
            assertThat(test(type, get("/type?time-only=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void timeOnlyNok() throws Exception {
        for (final String value : new String[]{"01:02", "2016-02-03", "02-03-04"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?time-only=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'time-only' on action(GET /type) - Value '" + value + "': Provided value " + value + " is not compliant with the format time_only provided in rfc3339"));
        }
    }

    @Test
    @Ignore("https://github.com/raml-org/raml-java-parser/issues/314")
    public void datetimeOnlyOk() throws Exception {
        for (final String value : new String[]{"2016-02-03T01:02:03", "2016-02-03T01:02:03.4", "2016-02-03T01:02:03.45", "2016-02-03T01:02:03.456"}) {
            assertThat(test(type, get("/type?datetime-only=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void datetimeOnlyNok() throws Exception {
        for (final String value : new String[]{"2016-02-03", "2016-02-03T01:02:03Z", "2016-02-03T01:02:03Z+02", "2016-02-03T01:02:03 +02"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?datetime-only=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'datetime-only' on action(GET /type) - Value '" + value + "': Provided value " + value + " is not compliant with the format datetime_only provided in rfc3339"));
        }
    }

    @Test
    public void datetimeOk() throws Exception {
        for (final String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertThat(test(type, get("/type?datetime=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void datetimeNok() throws Exception {
        for (final String value : new String[]{"28 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:56", "Fri, 28 Feb 2014"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?datetime=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'datetime' on action(GET /type) - Value '" + value + "': Provided value " + value + " is not compliant with the format datetime provided in rfc2616"));
        }
    }

    @Test
    public void stringOk() throws Exception {
        for (final String value : new String[]{"aa", "12345"}) {
            assertThat(test(type, get("/type?string=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void stringNok() throws Exception {
        assertOneRequestViolationThat(
                type,
                get("/type?string=a"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'string' on action(GET /type) - Value 'a': Expected min length 2"));
        assertOneRequestViolationThat(
                type,
                get("/type?string=123456"),
                jsonResponse(200, "\"hula\""),
                equalTo("Query parameter 'string' on action(GET /type) - Value '123456': Expected max length 5"));
    }

    @Test
    public void enumOk() throws Exception {
        for (final String value : new String[]{"a", "b"}) {
            assertThat(test(type, get("/type?enum=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void enumNok() throws Exception {
        for (final String value : new String[]{"", "ab", "c"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?enum=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'enum' on action(GET /type) - Value '" + value + "': Invalid element " + value + "."));
        }
    }

    @Test
    public void multiTypeOk() throws Exception {
        for (final String value : new String[]{"5", "666", "a", "b"}) {
            assertThat(test(type, get("/type?multi=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
    }

    @Test
    public void multiTypeNok() throws Exception {
        for (final String value : new String[]{"4", "4.5", "c"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?multi=" + value),
                    jsonResponse(200, "\"hula\""),
                    startsWith("Query parameter 'multi' on action(GET /type) - Value '" + value + "': Invalid element " + value + "."));
        }
    }

    @Test
    public void simplePattern() throws Exception {
        for (final String value : new String[]{"12/a", "00/y"}) {
            assertThat(test(type, get("/type?pattern1=" + value), jsonResponse(200, "\"hula\"")),
                    hasNoViolations());
        }
        for (final String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneRequestViolationThat(
                    type,
                    get("/type?pattern1=" + value),
                    jsonResponse(200, "\"hula\""),
                    equalTo("Query parameter 'pattern1' on action(GET /type) - Value '" + value + "': Invalid value '" + value + "'. Expected \\d{2}/[a-y]"));
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
        assertThat(test(type, post("/empty"), response(201, "", null)),
                hasNoViolations());
        assertThat(test(type, post("/empty"), response(202, "", "a/b")),
                hasNoViolations());
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
