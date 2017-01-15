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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static guru.nidi.ramltester.util.TestUtils.violations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ServletTest extends ServerTest {
    private CloseableHttpClient client;
    private static TestFilter testFilter;

    @BeforeClass
    public static void setupClass() {
        testFilter = new TestFilter();
    }

    @Before
    public void setup() {
        client = HttpClientBuilder.create().build();
    }

    @Test
    public void testServletOk() throws IOException {
        final HttpGet get = new HttpGet(url("data"));
        final CloseableHttpResponse response = client.execute(get);
        assertEquals("\"json string\"", EntityUtils.toString(response.getEntity()));
        assertTrue(testFilter.report.isEmpty());
    }

    @Test
    public void testServletNok() throws IOException {
        final HttpGet get = new HttpGet(url("data?param=bu"));
        final CloseableHttpResponse response = client.execute(get);
        assertEquals("illegal json", EntityUtils.toString(response.getEntity()));

        assertEquals(violations("Query parameter 'param' on action(GET /data) is not defined"),
                testFilter.report.getRequestViolations());

        assertEquals(violations("Body does not match schema for action(GET /data) response(200) mime-type('abc/xyz+json')\n" +
                        "Content: illegal json\n" +
                        "Messages:\n- Schema invalid: Unrecognized token 'illegal': was expecting ('true', 'false' or 'null')\n" +
                        " at [Source: Body; line: 1, column: 8]"),
                testFilter.report.getResponseViolations());
    }

    private static class TestFilter implements Filter {
        private static final RamlDefinition definition = RamlLoaders
                .fromClasspath(SimpleTest.class).load("simple.raml")
                .assumingBaseUri("http://nidi.guru/raml/v1");
        private RamlReport report;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            report = definition.testAgainst(request, response, chain);
        }

        @Override
        public void destroy() {
        }
    }

    private static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            final PrintWriter out = resp.getWriter();
            out.write(req.getParameter("param") == null ? "\"json string\"" : "illegal json");
            out.flush();
        }
    }

    @Override
    protected void init(Context ctx) {
        final FilterDef filterDef = new FilterDef();
        filterDef.setFilter(testFilter);
        filterDef.setFilterName("filter");
        ctx.addFilterDef(filterDef);

        final FilterMap filterMap = new FilterMap();
        filterMap.addServletName("app");
        filterMap.addURLPattern("/*");
        filterMap.setFilterName("filter");
        ctx.addFilterMap(filterMap);

        Tomcat.addServlet(ctx, "app", new TestServlet());
        ctx.addServletMapping("/*", "app");
    }
}