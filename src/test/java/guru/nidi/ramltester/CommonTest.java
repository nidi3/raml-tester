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

import guru.nidi.ramltester.core.RamlViolationException;
import guru.nidi.ramltester.junit.RamlMatchers;
import guru.nidi.ramltester.spring.SpringMockRamlRequest;
import guru.nidi.ramltester.spring.SpringMockRamlResponse;
import guru.nidi.ramltester.v10.SimpleTest;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import static guru.nidi.ramltester.util.TestUtils.violations;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class CommonTest extends HighlevelTestBase {
    private static final RamlDefinition simple = RamlLoaders.fromClasspath(SimpleTest.class).load("simple.raml");

    @Test
    public void failFastTest() throws Exception {
        try {
            simple.failFast().testAgainst(
                    new SpringMockRamlRequest(get("/base/path/noexisting").buildRequest(new MockServletContext())),
                    new SpringMockRamlResponse(jsonResponse(200)));
            fail("Should throw exception");
        } catch (RamlViolationException e) {
            assertEquals(violations("Request URL http://localhost/base/path/noexisting does not match base URI http://nidi.guru/raml/{version}"),
                    e.getReport().getRequestViolations());
            assertThat(e.getReport(), RamlMatchers.responseChecks());
            assertThat(e.getReport(), RamlMatchers.validates());
        }
    }

}
