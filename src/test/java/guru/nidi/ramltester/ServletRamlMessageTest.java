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

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import guru.nidi.ramltester.util.FileValue;
import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class ServletRamlMessageTest extends ServerTest {
    private CloseableHttpClient client;
    private static TestFilter testFilter;
    private static HttpServlet testServlet, gzipTestServlet;
    private static MessageTester tester;
    private static BlockingQueue<Error> error = new ArrayBlockingQueue<>(1);
    private static Error OK = new Error() {
    };

    @BeforeClass
    public static void setupClass() {
        testFilter = new TestFilter();
        testServlet = new TestServlet();
        gzipTestServlet = new GzipTestServlet();
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
    public void base() throws Exception {
        final HttpGet get = new HttpGet(url("test/more?param=value&param=v2"));
        get.addHeader("header", "pedro");
        execute(get, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) {
                assertEquals(0, ramlRequest.getContent().length);
                assertEquals(null, ramlRequest.getContentType());
                assertEquals(Arrays.asList("pedro"), ramlRequest.getHeaderValues().get("header"));
                assertEquals("GET", ramlRequest.getMethod());
                assertEquals(new Values().addValue("param", "value").addValue("param", "v2"), ramlRequest.getQueryValues());
                assertEquals(url("test/more"), ramlRequest.getRequestUrl(null, false));
                assertEquals("https://base/more", ramlRequest.getRequestUrl("https://base", false));

                assertEquals(0, ramlResponse.getContent().length);
                assertEquals(null, ramlResponse.getContentType());
                assertEquals(new Values().addValue("resHeader", "hula"), ramlResponse.getHeaderValues());
                assertEquals(222, ramlResponse.getStatus());
            }
        });
    }

    @Test
    public void content() throws Exception {
        final HttpPost post = new HttpPost(url("test/more"));
        final HttpEntity entity = new ByteArrayEntity(new byte[]{65, 66, 67});
        post.setEntity(entity);

        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals("ABC", stringOf(ramlRequest.getContent()));
                assertEquals("ABC", stringOf(ramlResponse.getContent()));
            }
        });
    }

    @Test
    public void urlEncodedForm() throws Exception {
        final HttpPost post = new HttpPost(url("test/more"));
        final HttpEntity entity = new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("param", "value"),
                new BasicNameValuePair("param", "v2"),
                new BasicNameValuePair("p2", "äöü+$% ")));
        post.setEntity(entity);

        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                final Values values = new Values()
                        .addValue("param", "value")
                        .addValue("param", "v2")
                        .addValue("p2", "äöü+$% ");
                assertEquals(values, ramlRequest.getFormValues());
                assertEquals("param=value&param=v2&p2=" + URLEncoder.encode("äöü+$% ", "iso-8859-1"), stringOf(ramlResponse.getContent()));
            }
        });
    }

    @Test
    public void multipartForm() throws Exception {
        final HttpPost post = new HttpPost(url("test/more"));
        final HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("binary", new byte[]{65, 66, 67}, ContentType.APPLICATION_OCTET_STREAM, "filename")
                .addTextBody("param", "value")
                .addTextBody("param", "v2")
                .addTextBody("p2", "äöü+$% ")
                .build();
        post.setEntity(entity);

        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                final Values values = new Values()
                        .addValue("binary", new FileValue())
                        .addValue("param", "value")
                        .addValue("param", "v2")
                        .addValue("p2", "äöü+$% ");
                assertEquals(values, ramlRequest.getFormValues());
            }
        });
    }

    @Test
    public void includeServletPathNoPathInfo() throws Exception {
        final HttpPost post = new HttpPost(url("test"));
        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals(baseUrlWithPort() + "/test", ramlRequest.getRequestUrl(null, true));
                assertEquals("http://server/servlet/path/test", ramlRequest.getRequestUrl("http://server/servlet/path", true));
            }
        });
    }

    @Test
    public void includeServletPathWithPathInfo() throws Exception {
        final HttpPost post = new HttpPost(url("test/info"));
        execute(post, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals(baseUrlWithPort() + "/test/info", ramlRequest.getRequestUrl(null, true));
                assertEquals("http://server/servlet/path/test/info", ramlRequest.getRequestUrl("http://server/servlet/path", true));
            }
        });
    }

    @Test
    public void gzip() throws Exception {
        final HttpGet get = new HttpGet(url("gzip/path"));

        execute(get, new MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals("Gzip works!", new String(ramlResponse.getContent()));
            }
        });
    }

    private String stringOf(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "iso-8859-1");
    }

    private void execute(HttpUriRequest request, MessageTester messageTester) throws IOException, InterruptedException {
        tester = messageTester;
        error.clear();
        client.execute(request);
        final Error e = error.poll(100, TimeUnit.MILLISECONDS);
        if (e == null) {
            fail("Got no response");
        }
        if (e != OK) {
            throw e;
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
                tester.test((HttpServletRequest) request, (HttpServletResponse) response, req, res);
                error.add(OK);
            } catch (Error e) {
                error.add(e);
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

    private static class GzipTestServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.addHeader("Content-Encoding", "gzip");
            res.setStatus(200);
            final GZIPOutputStream gzipOut = new GZIPOutputStream(res.getOutputStream());
            gzipOut.write("Gzip works!".getBytes());
            gzipOut.finish();
            res.getOutputStream().flush();
        }
    }

    @Override
    protected void init(Context ctx) {
        final FilterDef filterDef = new FilterDef();
        filterDef.setFilter(testFilter);
        filterDef.setFilterName("filter");
        ctx.addFilterDef(filterDef);

        final FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern("/*");
        filterMap.setFilterName("filter");
        ctx.addFilterMap(filterMap);

        Tomcat.addServlet(ctx, "test", testServlet);
        Tomcat.addServlet(ctx, "gzip", gzipTestServlet);
        ctx.addServletMapping("/test/*", "test");
        ctx.addServletMapping("/gzip/*", "gzip");
    }
}