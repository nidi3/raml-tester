/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.model.*;
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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.*;

import javax.servlet.http.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ServletRamlMessageTest extends ServerTest {
    private static TestFilter testFilter;
    private static HttpServlet testServlet, gzipTestServlet;

    @BeforeClass
    public static void setupClass() {
        testFilter = new TestFilter();
        testServlet = new EchoServlet();
        gzipTestServlet = new GzipTestServlet();
    }

    @Before
    public void setup() {
        testFilter.client = HttpClientBuilder.create().build();
    }

    @Test
    public void base() throws Exception {
        final HttpGet get = new HttpGet(url("test/more?param=value&param=v2"));
        get.addHeader("header", "pedro");
        testFilter.execute(get, new TestFilter.MessageTester() {
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

        testFilter.execute(post, new TestFilter.MessageTester() {
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

        testFilter.execute(post, new TestFilter.MessageTester() {
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

        testFilter.execute(post, new TestFilter.MessageTester() {
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
        testFilter.execute(post, new TestFilter.MessageTester() {
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
        testFilter.execute(post, new TestFilter.MessageTester() {
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

        testFilter.execute(get, new TestFilter.MessageTester() {
            @Override
            public void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException {
                assertEquals("Gzip works!", new String(ramlResponse.getContent()));
            }
        });
    }

    private String stringOf(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "iso-8859-1");
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