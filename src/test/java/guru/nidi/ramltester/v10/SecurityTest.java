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

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.core.RamlReport;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
                get("/sec2?access_token=bla").header("Authorization2", "blu"),
                response(401, "", null)));
    }

    @Test
    public void allowSecurityElementsInLocalGlobalSecured() throws Exception {
        assertNoViolations(test(
                global,
                get("/sec12").header("AuthorizationOpt", "blu"),
                response(200, "", null)));
    }

    @Test
    public void dontAllowMixSecuritySchemas() throws Exception {
        assertRequestViolationsThat(test(
                global,
                get("/sec12").header("AuthorizationOpt", "1").header("Authorization2", "2"),
                response(200, "", null)),
                either(is(equalTo("Header 'AuthorizationOpt' on action(GET /sec12) is not defined")))
                        .or(is(equalTo("Header 'Authorization2' on action(GET /sec12) is not defined")))
        );
    }

    @Test
    public void dontEliminateUniqueSecurityScheme() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/uniqueSec").header("AuthorizationOpt", "blu"),
                response(200, "", null)),
                equalTo("Header 'AuthorizationReq' on action(GET /uniqueSec) is required but not found"));
    }

    @Test
    public void showAmbiguousSecurityResolutionWithNull() throws Exception {
        final RamlReport report = test(
                local,
                get("/optSec").header("AuthorizationOpt", "blu"),
                response(200, "", null));
        assertEquals(Arrays.asList(
                "Assuming security scheme 'null': Header 'AuthorizationOpt' on action(GET /optSec) is not defined",
                "Assuming security scheme 'x-other': Header 'AuthorizationReq' on action(GET /optSec) is required but not found"),
                report.getRequestViolations().asList());
    }

    @Test
    public void showAmbiguousSecurityResolution() throws Exception {
        final RamlReport report = test(
                local,
                get("/doubleSec").header("AuthorizationOpt", "blu"),
                response(200, "", null));
        assertEquals(Arrays.asList(
                "Assuming security scheme 'OAuth 2.0': Header 'AuthorizationOpt' on action(GET /doubleSec) is not defined",
                "Assuming security scheme 'x-other': Header 'AuthorizationReq' on action(GET /doubleSec) is required but not found"),
                report.getRequestViolations().asList());
    }

    @Test
    public void showOnlyBestSecurityResolution() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/doubleSec?access_token=a").header("Authorization2", "blu").header("AuthorizationReq", "s"),
                response(200, "", null)),
                equalTo("Assuming security scheme 'OAuth 2.0': Header 'AuthorizationReq' on action(GET /doubleSec) is not defined"));
    }

    @Test
    public void allowSecurityElementsInLocalSecured() throws Exception {
        assertNoViolations(test(
                local,
                get("/sec?access_token=bla").header("Authorization2", "blu"),
                response(401, "", null)));
    }

    @Test
    public void dontAllowSecurityHeaderInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/unsec").header("Authorization2", "blu"),
                response(200, "", null)),
                equalTo("Header 'Authorization2' on action(GET /unsec) is not defined"));
    }

    @Test
    public void dontAllowSecurityQueryInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/unsec?access_token=bla"),
                response(200, "", null)),
                equalTo("Query parameter 'access_token' on action(GET /unsec) is not defined"));
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
