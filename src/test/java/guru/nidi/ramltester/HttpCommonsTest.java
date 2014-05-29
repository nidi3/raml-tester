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

import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
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
public class HttpCommonsTest extends ServerTest {
    private RamlHttpClient client;

    @Before
    public void setup() {
        client = RamlLoaders
                .fromClasspath(SimpleTest.class).load("simple.raml")
                .assumingBaseUri("http://nidi.guru/raml/v1")
                .createHttpClient();
    }

    @Override
    protected int port() {
        return 8082;
    }

    @Test
    public void testServletOk() throws IOException {
        final HttpGet get = new HttpGet(url("data"));
        final HttpResponse response = client.execute(get);
        assertEquals("\"json string\"", EntityUtils.toString(response.getEntity()));
        assertTrue(client.getLastReport().isEmpty());
    }

    @Test
    public void testServletNok() throws IOException {
        final HttpGet get = new HttpGet(url("data?param=bu"));
        final HttpResponse response = client.execute(get);
        assertEquals("illegal json", EntityUtils.toString(response.getEntity()));

        final RamlViolations requestViolations = client.getLastReport().getRequestViolations();
        assertEquals(1, requestViolations.size());
        assertThat(requestViolations.iterator().next(), equalTo("Query parameter 'param' on action(GET /data) is not defined"));

        final RamlViolations responseViolations = client.getLastReport().getResponseViolations();
        assertEquals(1, responseViolations.size());
        assertThat(responseViolations.iterator().next(),
                startsWith("Response content does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                        "Content: illegal json\n" +
                        "Message: Schema invalid: ")
        );
    }

    @Test
    public void notSending() throws IOException {
        final HttpGet get = new HttpGet(url("data"));
        final HttpResponse response = client.notSending().execute(get);
        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
        assertEquals(null, response.getEntity());
        assertTrue(client.getLastReport().isEmpty());
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
        Tomcat.addServlet(ctx, "app", new TestServlet());
        ctx.addServletMapping("/*", "app");
    }
}