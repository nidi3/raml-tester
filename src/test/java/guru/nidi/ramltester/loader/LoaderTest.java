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
package guru.nidi.ramltester.loader;

import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Date;

import static guru.nidi.ramltester.util.TestUtils.getEnv;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 *
 */
public class LoaderTest {
    @Test
    public void classPathOk() throws IOException {
        final InputStream in = new ClassPathRamlLoader("guru/nidi/ramltester").fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void classPathWithEndSlash() throws IOException {
        final InputStream in = new ClassPathRamlLoader("guru/nidi/ramltester/").fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void emptyBaseClassPath() throws IOException {
        final InputStream in = new ClassPathRamlLoader().fetchResource("guru/nidi/ramltester/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void jarInClassPath() {
        assertNotNull(new ClassPathRamlLoader("org/junit").fetchResource("Test.class", -1));
    }

    @Test
    public void jarInClassPathNotModified() {
        assertNull(new ClassPathRamlLoader("org/junit").fetchResource("Test.class", new Date(130, 0, 0).getTime() - 1));
    }

    @Test
    public void fileInClassPathNotModified() throws IOException {
        final long mod = new File("target/test-classes/guru/nidi/ramltester/simple.raml").lastModified();
        assertNull(new ClassPathRamlLoader("guru/nidi/ramltester").fetchResource("simple.raml", mod + 1));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void classPathNok() {
        new ClassPathRamlLoader("guru/nidi/ramltester").fetchResource("bla", -1);
    }

    @Test
    public void fileOk() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        final InputStream in = new FileRamlLoader(new File(resource.getPath())).fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void fileNotModified() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        final long mod = new File(resource.getPath()).lastModified();
        assertNull(new FileRamlLoader(new File(resource.getPath())).fetchResource("simple.raml", mod + 1));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void fileNok() {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        new FileRamlLoader(new File(resource.getPath())).fetchResource("bla", -1);
    }

    @Test
    public void urlOk() throws IOException {
        final InputStream in = new UrlRamlLoader("http://en.wikipedia.org/wiki").fetchResource("Short", -1);
        assertStreamStart(in, "<!DOCTYPE html>");
    }


    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void urlNok() {
        new UrlRamlLoader("http://en.wikipedia.org").fetchResource("dfkjsdfhfs", -1);
    }

    @Test
    public void loadFile() throws IOException {
        final InputStream in = new FileRamlLoader(new File("src/test/resources/guru/nidi/ramltester")).fetchResource("simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithub() throws IOException {
        final InputStream in = new GithubRamlLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", -1);
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void publicGithubNotModified() throws IOException {
        assertNull(new GithubRamlLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(130, 0, 1).getTime()));
    }

    @Test
    public void publicGithubModified() throws IOException {
        final InputStream in = new GithubRamlLoader("nidi3/raml-tester").fetchResource("src/test/resources/guru/nidi/ramltester/simple.raml", new Date(100, 0, 1).getTime());
        assertStreamStart(in, "#%RAML 0.8");
    }

    @Test
    public void privateGithub() throws IOException {
        final InputStream in = new GithubRamlLoader(getEnv("GITHUB_TOKEN"), "nidi3/blog").fetchResource("README.md", -1);
        assertStreamStart(in, "blog");
    }

    private void assertStreamStart(InputStream in, String s) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            assertThat(reader.readLine(), equalTo(s));
        }
    }

}
