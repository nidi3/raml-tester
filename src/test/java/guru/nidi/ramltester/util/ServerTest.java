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

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public abstract class ServerTest {
    protected static final int PORT = 18765;
    private static Tomcat tomcat;
    private static Set<Class<?>> inited = new HashSet<>();
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
        return baseUrl() + ":" +PORT;
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
    }
}
