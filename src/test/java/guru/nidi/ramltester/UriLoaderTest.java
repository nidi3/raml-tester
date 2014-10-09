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

import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.util.TestUtils.getEnv;
import static org.junit.Assert.*;

/**
 *
 */
public class UriLoaderTest extends ServerTest {
    @Test
    public void uriRegex() {
        final Pattern ABSOLUTE_URI_PATTERN = Pattern.compile("([^:]+)://(.+)/([^/]+)");
        final Matcher matcher = ABSOLUTE_URI_PATTERN.matcher("http://a/b/c");
        assertTrue(matcher.matches());
        assertEquals("http", matcher.group(1));
        assertEquals("a/b", matcher.group(2));
        assertEquals("c", matcher.group(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteWithoutProtocol() {
        RamlLoaders.absolutely().load("relative.raml");
    }

    @Test
    public void file() {
        assertNotNull(RamlLoaders.absolutely().load("file://" + getClass().getResource("simple.raml").getFile()));
    }

    @Test
    public void classpath() {
        assertNotNull(RamlLoaders.absolutely().load("classpath://guru/nidi/ramltester/simple.raml"));
    }

    @Test
    public void url() {
        assertNotNull(RamlLoaders.absolutely().load("http://localhost:" + port() + "/deliver/form.raml"));
    }

    @Test
    public void apiPortal() {
        assertNotNull(RamlLoaders.absolutely().load("apiportal://" + getEnv("API_PORTAL_USER") + ":" + getEnv("API_PORTAL_PASS") + "/test.raml"));
    }

    @Test
    @Ignore
    public void apiDesigner() {
        assertNotNull(RamlLoaders.absolutely().load("apidesigner://todo"));
    }

    @Test
    public void ramlWithAbsoluteIncludes() {
        assertNotNull(RamlLoaders.absolutely().load("http://localhost:" + port() + "/deliver/load.raml"));
    }

    private static class FileDeliveringServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final URL in = getClass().getResource(req.getPathInfo().substring(1));
            if (in == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Files.copy(new File(in.getFile()).toPath(), resp.getOutputStream());
            }
        }
    }

    @Override
    protected int port() {
        return 8085;
    }

    @Override
    protected void init(Context ctx) {
        Tomcat.addServlet(ctx, "app", new FileDeliveringServlet());
        ctx.addServletMapping("/deliver/*", "app");
    }
}
