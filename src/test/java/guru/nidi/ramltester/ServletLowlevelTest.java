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

import guru.nidi.ramltester.core.RamlRequest;
import guru.nidi.ramltester.core.RamlResponse;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import guru.nidi.ramltester.util.ServerTest;
import guru.nidi.ramltester.util.Values;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ServletLowlevelTest extends ServerTest {
    private CloseableHttpClient client;
    private static TestFilter testFilter;
    private static TestServlet testServlet;
    private static MessageTester tester;
    private static Error error;

    @BeforeClass
    public static void setupClass() {
        testFilter = new TestFilter();
        testServlet = new TestServlet();
    }

    @Before
    public void setup() {
        client = HttpClientBuilder.create().build();
    }

    @Override
    protected int port() {
        return 8084;
    }

    interface MessageTester {
        void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException;
    }

    @Test
    public void base() throws IOException {
        final HttpGet get = new HttpGet(url("path/more?param=value&param=v2"));
        get.addHeader("header", "pedro");
        execute(get, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) {
                assertEquals("", ramlRequest.getContent());
                assertEquals(null, ramlRequest.getContentType());
                assertEquals(Arrays.asList("pedro"), ramlRequest.getHeaderValues().get("header"));
                assertEquals("GET", ramlRequest.getMethod());
                assertEquals(new Values().addValue("param", "value").addValue("param", "v2"), ramlRequest.getQueryValues());
                assertEquals(url("path/more"), ramlRequest.getRequestUrl(null));
                assertEquals("https://base/path/more", ramlRequest.getRequestUrl("https://base"));

                assertEquals("", ramlResponse.getContent());
                assertEquals(null, ramlResponse.getContentType());
                assertEquals(new Values().addValue("resHeader", "hula"), ramlResponse.getHeaderValues());
                assertEquals(222, ramlResponse.getStatus());
            }
        });
    }

    @Test
    public void content() throws IOException {
        final HttpPost post = new HttpPost(url("path/more"));
        final HttpEntity entity = new ByteArrayEntity(new byte[]{65, 66, 67});
        post.setEntity(entity);

        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals("ABC", ramlRequest.getContent());
                assertEquals("ABC", ramlResponse.getContent());
            }
        });
    }

    private void execute(HttpUriRequest request, MessageTester messageTester) throws IOException {
        tester = messageTester;
        client.execute(request);
        if (error != null) {
            throw error;
        }
    }

    private static class TestFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            final ServletRamlRequest req = new ServletRamlRequest((HttpServletRequest) request);
            final ServletRamlResponse res = new ServletRamlResponse((HttpServletResponse) response);
            chain.doFilter(req, res);
            try {
                error = null;
                tester.test((HttpServletRequest) request, (HttpServletResponse) response, req, res);
            } catch (Error e) {
                error = e;
            }
        }

        @Override
        public void destroy() {
        }
    }

    private static class TestServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.addHeader("resHeader", "hula");
            res.setStatus(222);
            byte[] buf = new byte[1000];
            try (final ServletInputStream in = req.getInputStream();
                 final ServletOutputStream out = res.getOutputStream()) {
                int read;
                while ((read = in.read(buf)) > 0) {
                    out.write(buf, 0, read);
                }
                out.flush();
            }
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

        Tomcat.addServlet(ctx, "app", testServlet);
        ctx.addServletMapping("/*", "app");
    }
}