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
package guru.nidi.ramltester.apidesigner;

import guru.nidi.ramltester.loader.RamlLoader;
import guru.nidi.ramltester.loader.RamlLoaderRamlParserResourceLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.IOException;

import static guru.nidi.ramltester.util.TestUtils.getEnv;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class ApiRamlLoaderTest {
    private ApiRamlLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = new ApiRamlLoader(getEnv("API_PORTAL_USER"), getEnv("API_PORTAL_PASS"));
    }

    @Test
    @Ignore
    public void fromApiPortalOk() throws IOException {
        assertNotNull(new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(loader)).build("test.raml"));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void fromApiPortalUnknownFile() throws IOException {
        new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(loader)).build("huhuhuhuhu.raml");
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void fromApiPortalUnknownUser() throws IOException {
        new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(new ApiRamlLoader("wwwwww", "blalbulbi"))).build("test.raml");
    }
}
