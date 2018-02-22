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
package guru.nidi.ramltester.util;

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.fail;

public abstract class ServerTest {
    protected static final int PORT = 18765;
    private static Tomcat tomcat;
    private static final Set<Class<?>> inited = new HashSet<>();
    private final static JarScanner NO_SCAN = new JarScanner() {
        @Override
        public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
        }
    };

    @Before
    public void initImpl() throws LifecycleException, ServletException {
        if (!inited.contains(getClass())) {
            inited.add(getClass());
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            tomcat = new Tomcat();
            tomcat.setPort(PORT);
            tomcat.setBaseDir(".");
            final Context ctx = tomcat.addWebapp("", "src/test");
            ctx.setJarScanner(NO_SCAN);
            ((Host) ctx.getParent()).setAppBase("");

            init(ctx);

            tomcat.start();
        }
    }

    protected void init(Context ctx) {
    }

    protected String url(String path) {
        return baseUrlWithPort() + "/" + path;
    }

    protected String baseUrlWithPort() {
        return baseUrl() + ":" + PORT;
    }

    protected String baseUrl() {
        return "http://localhost";
    }

    @AfterClass
    public static void stopTomcat() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }

    public static class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            if (req.getParameter("empty") == null) {
                res.setContentType("application/json");
                final PrintWriter out = res.getWriter();
                out.write(req.getParameter("param") == null ? "\"json string\"" : "illegal json");
                out.flush();
            } else {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    public static class EchoServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.addHeader("resHeader", "hula");
            res.setStatus(222);
            final byte[] buf = new byte[1000];
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

    public static class GzipTestServlet extends HttpServlet {
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

    public static class TestFilter implements Filter {
        public CloseableHttpClient client;
        private static MessageTester tester;
        private static final BlockingQueue<Error> error = new ArrayBlockingQueue<>(1);
        private static final Error OK = new Error() {
        };

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        public interface MessageTester {
            void test(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RamlRequest ramlRequest, RamlResponse ramlResponse) throws IOException;
        }

        public void execute(HttpUriRequest request, MessageTester messageTester) throws IOException, InterruptedException {
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

}
