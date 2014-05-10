package guru.nidi.ramltester;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static guru.nidi.ramltester.spring.RamlResultMatchers.requestResponse;

/**
 *
 */
@Controller
public class UriTest extends TestBase {
    private RamlDefinition uri = TestRaml.load("uri.raml").fromClasspath(getClass());
    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(this).build();
    }

    @RequestMapping(value = {"/raml/v1/{def}/{type}", "/v1/{def}/{type}", "/{def}/{type}"})
    @ResponseBody
    public HttpEntity<String> test() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new HttpEntity<>(headers);
    }

    @Test(expected = AssertionError.class)
    public void standardServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd"))
                .andExpect(requestResponse().matchesRaml(uri));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd"))
                .andExpect(requestResponse().withServletUri("invalid").matchesRaml(uri));
    }

    @Test
    public void correctServletUri() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/raml/v1/undefd/type"))
                .andExpect(requestResponse().withServletUri("http://nidi.guru").matchesRaml(uri));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/undefd/type"))
                .andExpect(requestResponse().withServletUri("http://nidi.guru/raml").matchesRaml(uri));

        mockMvc.perform(MockMvcRequestBuilders.get("/undefd/type"))
                .andExpect(requestResponse().withServletUri("http://nidi.guru/raml/v1").matchesRaml(uri));
    }

    @Test
    public void preferSubResourceWithLessVariables() throws Exception {
        assertNoViolations(
                uri,
                get("/undefined/type/sub"),
                jsonResponse(201));
        assertNoViolations(
                uri,
                get("/undefined/type/1"),
                jsonResponse(202));
    }

    @Test
    public void checkUriParameters() throws Exception {
        assertOneRequestViolationThat(
                uri,
                get("/undefined/type/other"),
                jsonResponse(202),
                allOf(startsWith("URI parameter 'undefined'"), endsWith("Value 'other' is not a valid integer")));
        assertNoViolations(
                uri,
                get("/undefined/type/other/sub"),
                jsonResponse(203));
    }
}
