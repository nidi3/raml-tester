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

import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.jaxrs.JaxrsContextRamlRequest;
import guru.nidi.ramltester.jaxrs.JaxrsContextRamlResponse;
import guru.nidi.ramltester.junit.ExpectedUsage;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static guru.nidi.ramltester.util.TestUtils.stringArrayMapOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(Parameterized.class)
public class JaxrsTest extends ServerTest {
    private static final RamlDefinition raml = RamlLoaders.fromClasspath(JaxrsTest.class).load("jaxrs.raml");
    private final String uri = "http://localhost:" + port();
    private final Client client;

    private static MultiReportAggregator aggregator = new MultiReportAggregator();

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator.usageProvider(raml));

    public JaxrsTest(Client client) {
        this.client = client;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Client> clients() {
        return Arrays.asList(
                JerseyClientBuilder.createClient(),
                new ResteasyClientBuilder().build());
    }

    @Override
    protected int port() {
        return 8086;
    }

    @Test
    public void model() {
        final RamlRequest[] request = new RamlRequest[1];
        final RamlResponse[] response = new RamlResponse[1];

        final CheckingWebTarget checking = raml.createWebTarget(client.target(uri)).aggregating(aggregator);

        checking.register(new ClientResponseFilter() {
            @Override
            public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
                request[0] = new JaxrsContextRamlRequest(requestContext);
                response[0] = new JaxrsContextRamlResponse(responseContext);
            }
        });

        final Invocation invocation = checking.path("/app/path").queryParam("qp", "true")
                .request().header("h", "h2")
                .buildPost(Entity.entity("data", "text/plain"));

        final String s = invocation.invoke(String.class);
        assertEquals("\"json string\"", s);

        assertEquals("POST", request[0].getMethod());
        assertEquals(stringArrayMapOf("qp", "true"), request[0].getQueryValues());
        assertEquals(Arrays.asList("h2"), request[0].getHeaderValues().get("h"));
        assertEquals("http://localhost:" + port() + "/app/path", request[0].getRequestUrl(null));
        assertEquals("text/plain", request[0].getContentType());
        assertArrayEquals("data".getBytes(), request[0].getContent());

        assertEquals(200, response[0].getStatus());
        assertEquals(Arrays.asList("true"), response[0].getHeaderValues().get("res"));
        assertThat(response[0].getContentType(), startsWith("application/json"));
        assertArrayEquals("\"json string\"".getBytes(), response[0].getContent());
    }

    @Test
    public void client() {
        final CheckingWebTarget checking = raml.createWebTarget(client.target(uri));
        checking.path("/app/path").queryParam("qp", "true")
                .request().header("h", "h2")
                .post(Entity.entity("data", "text/plain"));
        assertTrue(checking.getLastReport().isEmpty());
    }

    @Override
    protected void init(Context ctx) {
        Tomcat.addServlet(ctx, "app", new TestServlet());
        ctx.addServletMapping("/app/path/*", "app");
    }

    private static class TestServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("d".getBytes()[0], req.getInputStream().read());
            resp.setContentType("application/json");
            resp.setHeader("res", "true");
            final PrintWriter out = resp.getWriter();
            out.write(req.getParameter("param") == null ? "\"json string\"" : "illegal json");
            out.flush();
        }
    }
}
