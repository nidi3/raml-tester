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

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;

/**
 *
 */
public class SecurityTest extends HighlevelTestBase {
    private static RamlLoaders base = RamlLoaders.fromClasspath(SecurityTest.class);
    private static RamlDefinition
            global = base.load("global-security.raml"),
            local = base.load("local-security.raml"),
            undef = base.load("undefined-security.raml");

    @Test
    public void allowSecurityElementsInGlobalSecured() throws Exception {
        assertNoViolations(test(
                global,
                get("/sec2").param("access_token", "bla").header("Authorization2", "blu"),
                response(401, "", null)));
    }

    @Test
    public void allowSecurityElementsInLocalGlobalSecured() throws Exception {
        assertNoViolations(test(
                global,
                get("/sec12").header("Authorization1", "blu"),
                response(200, "", null)));
    }
    @Test
    public void dontAllowMixSecuritySchemas() throws Exception {
        assertRequestViolationsThat(test(
                        global,
                        get("/sec12").header("Authorization1", "1").header("Authorization2", "2"),
                        response(200, "", null)),
        		//Headers from MockHttpServletRequest are build from a Set. No Order (breakes on mac jvm) 
        		either(is(
                equalTo("Header 'Authorization1' on action(GET /sec12) is not defined"))).or(is(
                equalTo("Header 'Authorization2' on action(GET /sec12) is not defined")))
        );
        
    }


    @Test
    public void allowSecurityElementsInLocalSecured() throws Exception {
        assertNoViolations(test(
                local,
                get("/sec").param("access_token", "bla").header("Authorization2", "blu"),
                response(401, "", null)));
    }

    @Test
    public void dontAllowSecurityElementsInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                        local,
                        get("/unsec").param("access_token", "bla").header("Authorization2", "blu"),
                        response(200, "", null)),
                equalTo("Header 'Authorization2' on action(GET /unsec) is not defined"));
    }

    @Test
    public void allowSecurityWithoutDescribedBy() throws Exception {
        assertNoViolations(test(
                global,
                get("/undesc"),
                response(200, "", null)));
    }

    @Test
    //TODO should this test fail because of wrong securityScheme.type?
    public void allowWrongSecurityType() throws Exception {
        assertNoViolations(test(
                global,
                get("/type"),
                response(200, "", null)));
    }

    @Test
    public void undefinedGlobalSecuritySchema() throws Exception {
        assertOneRequestViolationThat(test(
                        undef,
                        get("/unsec"),
                        response(200, "", null)),
                equalTo("Security Scheme 'b' on Root definition is not defined"));
    }

    @Test
    public void undefinedResourceSecuritySchema() throws Exception {
        assertOneRequestViolationThat(test(
                        undef,
                        get("/sec"),
                        response(200, "", null)),
                equalTo("Security Scheme 'c' on resource(/sec) is not defined"));
    }

    @Test
    public void undefinedActionSecuritySchema() throws Exception {
        assertOneRequestViolationThat(test(
                        undef,
                        post("/sec"),
                        response(200, "", null)),
                equalTo("Security Scheme 'd' on action(POST /sec) is not defined"));
    }

}
