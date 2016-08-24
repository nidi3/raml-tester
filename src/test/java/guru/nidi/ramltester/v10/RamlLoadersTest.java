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
package guru.nidi.ramltester.v10;

import guru.nidi.Base;
import guru.nidi.ramltester.RamlLoaders;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RamlLoadersTest {
    @Test
    public void fileString() {
        assertTitle(RamlLoaders.fromFile("src/test/resources/guru/nidi/ramltester"), "simple.raml", "simple");
    }

    @Test
    public void file() {
        assertTitle(RamlLoaders.fromFile(new File("src/test/resources/guru/nidi/ramltester")), "simple.raml", "simple");
    }

    @Test
    public void baseClasspath() {
        assertTitle(RamlLoaders.fromClasspath(), "guru/nidi/ramltester/simple.raml", "simple");
    }

    @Test
    public void stringClasspath() {
        assertTitle(RamlLoaders.fromClasspath("guru/nidi/ramltester"), "simple.raml", "simple");
    }

    @Test
    public void classClasspath() {
        assertTitle(RamlLoaders.fromClasspath(RamlLoaders.class), "simple.raml", "simple");
    }

    @Test
    public void url() {
        assertTitle(RamlLoaders.fromUrl("https://raw.githubusercontent.com/nidi3/raml-tester/master/src/test/resources/guru/nidi/ramltester"), "simple.raml", "simple");
    }

    @Test
    @Ignore
    public void github() {
        assertTitle(RamlLoaders.fromGithub("nidi3", "raml-tester"), "src/test/resources/guru/nidi/ramltester/simple.raml", "simple");
    }

    @Test
    public void classpathAndFileString() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromFile("src/test/resources/guru/nidi");
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    @Test
    public void classpathAndFile() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromFile(new File("src/test/resources/guru/nidi"));
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    @Test
    public void classpathAndStringClasspath() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromClasspath("guru/nidi");
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    @Test
    public void classpathAndClasspath() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromClasspath(Base.class);
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    @Test
    public void classpathAndUrl() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromUrl("https://raw.githubusercontent.com/nidi3/raml-tester/master/src/test/resources/guru/nidi");
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    @Test
    @Ignore
    public void classpathAndGithub() {
        final RamlLoaders loaders = RamlLoaders.fromClasspath(RamlLoaders.class).andFromGithub("nidi3", "raml-tester/src/test/resources/guru/nidi");
        assertTitle(loaders, "simple.raml", "simple");
        assertTitle(loaders, "ramltester/simple.raml", "simple");
    }

    private void assertTitle(RamlLoaders loaders, String raml, String expected) {
        assertEquals(expected, loaders.load(raml).getRaml().title());
    }
}

