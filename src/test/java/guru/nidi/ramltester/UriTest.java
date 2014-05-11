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

import static org.hamcrest.CoreMatchers.equalTo;


/**
 *
 */
@Controller
public class UriTest extends HighlevelTestBase {
    private RamlDefinition api = TestRaml.load("uri.raml").fromClasspath(getClass());
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
                equalTo("URI parameter 'undefined' on resource(/{undefined}) : Value 'other' is not a valid integer"));
        assertNoViolations(
                api,
                get("/undefined/type/other/sub"),
                jsonResponse(203));
    }
}
