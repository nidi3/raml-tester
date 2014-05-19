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
package guru.nidi.ramltester;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolations;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

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

    @Override
    protected int port() {
        return 8080;
    }

    @Test
    public void testServletOk() throws IOException {
        final HttpGet get = new HttpGet("http://localhost:8080/data");
        final CloseableHttpResponse response = client.execute(get);
        assertEquals("\"json string\"", EntityUtils.toString(response.getEntity()));
        assertTrue(testFilter.report.isEmpty());
    }

    @Test
    public void testServletNok() throws IOException {
        final HttpGet get = new HttpGet("http://localhost:8080/data?param=bu");
        final CloseableHttpResponse response = client.execute(get);
        assertEquals("illegal json", EntityUtils.toString(response.getEntity()));

        final RamlViolations requestViolations = testFilter.report.getRequestViolations();
        assertEquals(1, requestViolations.size());
        assertThat(requestViolations.iterator().next(), equalTo("Query parameter 'param' on action(GET /data) is not defined"));

        final RamlViolations responseViolations = testFilter.report.getResponseViolations();
        assertEquals(1, responseViolations.size());
        assertThat(responseViolations.iterator().next(),
                startsWith("Response content does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                        "Content: illegal json\n" +
                        "Message: Schema invalid: ")
        );
    }

    private static class TestFilter implements Filter {
        private RamlDefinition definition = RamlTester.fromClasspath(SimpleTest.class).load("simple.raml");
        private RamlReport report;

        TestFilter() {
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            report = definition.testAgainst("http://nidi.guru/raml/v1", request, response, chain);
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