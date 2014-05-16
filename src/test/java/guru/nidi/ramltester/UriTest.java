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

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.hamcrest.CoreMatchers.equalTo;


/**
 *
 */
@Controller
public class UriTest extends HighlevelTestBase {
    private RamlDefinition api = TestRaml.fromClasspath(getClass()).load("uri.raml");
    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(this).build();
    }

    @RequestMapping(value = {"/raml/v1/{def}/{type}", "/v1/{def}/{type}", "/{def}/{type}", "/sub-raml/{a}/{b}/{c}/{d}"})
    @ResponseBody
    public HttpEntity<String> test() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new HttpEntity<>(headers);
    }

    @Test(expected = AssertionError.class)
    public void standardServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd"))
                .andExpect(api.matches());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd"))
                .andExpect(api.matches().assumingServletUri("invalid"));
    }

    @Test
    public void correctServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd/type"))
                .andExpect(api.matches().assumingServletUri("http://nidi.guru"));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/undefd/type"))
                .andExpect(api.matches().assumingServletUri("http://nidi.guru/raml"));

        mockMvc.perform(MockMvcRequestBuilders.get("/undefd/type"))
                .andExpect(api.matches().assumingServletUri("http://nidi.guru/raml/v1"));
    }

    @Test
    public void preferSubResourceWithLessVariables() throws Exception {
        assertNoViolations(
                api,
                get("/undefined/type/sub"),
                jsonResponse(201));
        assertNoViolations(
                api,
                get("/undefined/type/1"),
                jsonResponse(202));
    }

    @Test
    public void checkUriParameters() throws Exception {
        assertOneRequestViolationThat(
                api,
                get("/undefined/type/other"),
                jsonResponse(202),
                equalTo("URI parameter 'undefined' on resource(/type/{undefined}) : Value 'other' is not a valid integer"));
        assertNoViolations(
                api,
                get("/undefined/type/other/sub"),
                jsonResponse(203));
    }

    @Test
    public void overwrittenBaseUriParametersNok() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd/bu")).andReturn();
        assertOneViolationThat(
                api.testAgainst(mvcResult, "http://nidi.guru").getRequestViolations(),
                equalTo("BaseUri parameter 'host' on action(GET /bu) : Value 'nidi.guru' is not a member of enum '[bu-host]'"));
    }

    @Test
    public void overwrittenBaseUriParametersNok2() throws Exception {
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/sub-raml/v1/undefd/bu/sub")).andReturn();
        assertOneViolationThat(
                api.testAgainst(result, "http://sub-host").getRequestViolations(),
                equalTo("BaseUri parameter 'host' on action(GET /bu/sub) : Value 'sub-host' is not a member of enum '[sub-host-get]'"));
    }

    @Test
    public void overwrittenBaseUriParameters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd/bu"))
                .andExpect(api.matches().assumingServletUri("http://bu-host"));

        mockMvc.perform(MockMvcRequestBuilders.get("/sub-raml/v1/undefd/bu/sub"))
                .andExpect(api.matches().assumingServletUri("http://sub-host-get"));

    }
}
