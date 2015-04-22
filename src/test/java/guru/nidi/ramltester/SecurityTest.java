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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
public class SecurityTest extends HighlevelTestBase {
    private static RamlLoaders base = RamlLoaders.fromClasspath(SecurityTest.class);
    private static RamlDefinition
            global = base.load("global-security.raml"),
            local = base.load("local-security.raml");

    @Test
    public void allowSecurityElementsInGlobalSecured() throws Exception {
        assertNoViolations(test(
                global,
                get("/sec2").param("access_token", "bla").header("Authorization2", "blu"),
                jsonResponse(401, "", null)));
    }

    @Test
    public void allowSecurityElementsInLocalGlobalSecured() throws Exception {
        assertNoViolations(test(
                global,
                get("/sec12").header("Authorization1", "blu"),
                jsonResponse(200, "", null)));
    }

    @Test
    public void dontAllowMixSecuritySchemas() throws Exception {
        assertRequestViolationsThat(test(
                        global,
                        get("/sec12").header("Authorization1", "1").header("Authorization2", "2"),
                        jsonResponse(200, "", null)),
                equalTo("Header 'Authorization2' on action(GET /sec12) is not defined"),
                equalTo("Header 'Authorization1' on action(GET /sec12) is not defined")
        );
    }

    @Test
    public void allowSecurityElementsInLocalSecured() throws Exception {
        assertNoViolations(test(
                local,
                get("/sec").param("access_token", "bla").header("Authorization2", "blu"),
                jsonResponse(401, "", null)));
    }

    @Test
    public void dontAllowSecurityElementsInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                        local,
                        get("/unsec").param("access_token", "bla").header("Authorization2", "blu"),
                        jsonResponse(200, "", null)),
                equalTo("Header 'Authorization2' on action(GET /unsec) is not defined"));
    }

}
