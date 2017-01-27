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
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class HeaderTest extends HighlevelTestBase {
    private static final RamlDefinition header = RamlLoaders.fromClasspath(HeaderTest.class).load("header.raml");
    private static final SimpleReportAggregator aggregator = new SimpleReportAggregator();

    @ClassRule
    public static final ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Test
    public void undefinedRequestHeader() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                header,
                get("/data").header("a", "b"),
                jsonResponse(200, "\"hula\"")),
                equalTo("Header 'a' on action(GET /data) is not defined")
        );
    }

    @Test
    public void illegallyRepeatRequestHeader() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                header,
                get("/header").header("req", "1").header("req", "2"),
                jsonResponse(200, "\"hula\"")),
                equalTo("Header 'req' on action(GET /header) is not repeat but found repeatedly")
        );
    }

    @Test
    public void allowedRepeatRequestHeader() throws Exception {
        assertThat(
                test(aggregator, header,
                        get("/header").header("req", "xxx")
                                .header("string-array", "s1").header("string-array", "s2")
                                .header("int-array", "1").header("int-array", "2")
                                .header("array-of-string", "s1").header("array-of-string", "s2")
                                .header("array-of-int", "1").header("array-of-int", "2")
                                .header("array", "s1").header("array", "s2"),
                        jsonResponse(200, "\"hula\"")),
                hasNoViolations());
    }

    @Test
    public void missingRequiredRequestHeader() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                header,
                get("/header"),
                jsonResponse(200, "\"hula\"")),
                equalTo("Header 'req' on action(GET /header) is required but not found")
        );
    }

    @Test
    public void wildcardRequestHeader() throws Exception {
        assertThat(
                test(aggregator, header,
                        get("/header").header("x-bla", "1").header("x-hula", "2").header("req", "3"),
                        jsonResponse(200, "\"hula\"")),
                hasNoViolations());
    }

    @Test
    public void missingRequiredWildcardRequestHeader() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                header,
                get("/header/reqwild"),
                jsonResponse(200)),
                equalTo("Header 'x-{?}' on action(GET /header/reqwild) is required but not found")
        );
    }

    @Test
    public void existingRequiredWildcardRequestHeader() throws Exception {
        assertThat(
                test(aggregator, header,
                        get("/header/reqwild").header("x-", "w"),
                        jsonResponse(200)),
                hasNoViolations());
    }

    @Test
    public void undefinedResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("a", "b");
        assertOneResponseViolationThat(test(aggregator,
                header,
                get("/data"),
                response),
                equalTo("Header 'a' on action(GET /data) response(200) is not defined")
        );
    }

    @Test
    public void illegallyRepeatResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("req", "1");
        response.addHeader("req", "2");
        assertOneResponseViolationThat(test(aggregator,
                header,
                get("/resheader"),
                response),
                equalTo("Header 'req' on action(GET /resheader) response(200) is not repeat but found repeatedly")
        );
    }

    @Test
    public void allowedRepeatResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("rep", "1");
        response.addHeader("rep", "2");
        response.addHeader("req", "xxx");
        assertThat(test(aggregator, header, get("/resheader"), response),
                hasNoViolations());
    }

    @Test
    public void missingRequiredResponseHeader() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                header,
                get("/resheader"),
                jsonResponse(200, "\"hula\"")),
                equalTo("Header 'req' on action(GET /resheader) response(200) is required but not found")
        );
    }

    @Test
    public void wildcardResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("x-bla", "1");
        response.addHeader("x-hula", "2");
        response.addHeader("req", "3");
        assertThat(test(aggregator, header, get("/resheader"), response),
                hasNoViolations());
    }

    @Test
    public void missingRequiredWildcardResponseHeader() throws Exception {
        assertOneResponseViolationThat(test(aggregator,
                header,
                get("/resheader/reqwild"),
                jsonResponse(200)),
                equalTo("Header 'x-{?}' on action(GET /resheader/reqwild) response(200) is required but not found")
        );
    }

    @Test
    public void existingRequiredWildcardResponseHeader() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200);
        response.addHeader("x-", "w");
        assertThat(test(aggregator, header, get("/resheader/reqwild"), response),
                hasNoViolations());
    }

    @Test
    public void ignoreXheaders() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        response.addHeader("x-hula", "hop");
        assertThat(test(aggregator, header.ignoringXheaders(), get("/data").header("x-bla", "blu"), response),
                hasNoViolations());
    }

    @Test
    public void caseInsensitiveNames() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200);
        response.addHeader("x-INT", "6");
        assertThat(test(aggregator, header, get("/header/xint").header("x-INT", "5"), response),
                hasNoViolations());
    }

    @Test
    public void notIgnoreXrequestHeadersIfGiven() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                header.ignoringXheaders(),
                get("/header/xint").header("x-int", "blu").header("x-ig", "nix"),
                jsonResponse(200)),
                equalTo("Header 'x-int' on action(GET /header/xint) - Value 'blu': Invalid type String, expected Integer"));
    }

    @Test
    public void notIgnoreXresponseHeadersIfGiven() throws Exception {
        final MockHttpServletResponse response = jsonResponse(200);
        response.addHeader("x-int", "blu");
        response.addHeader("x-ig", "nix");
        assertOneResponseViolationThat(test(aggregator,
                header.ignoringXheaders(),
                get("/header/xint"),
                response),
                equalTo("Header 'x-int' on action(GET /header/xint) response(200) - Value 'blu': Invalid type String, expected Integer"));
    }

}
