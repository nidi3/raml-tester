package guru.nidi.ramltester;

import org.junit.Assert;
import org.junit.Test;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class RamlTesterTest {

    @Test
    public void testSimple() throws Exception {
        final MockHttpServletRequest request = MockMvcRequestBuilders.get("/data").buildRequest(new MockServletContext());
        final MockHttpServletResponse response = jsonResponse(200, "\"hula\"");
        final RamlViolations violations = new RamlTester().test(new RamlDocumentBuilder().build(getClass().getResourceAsStream("simple.raml"), "simple"), request, response);
        Assert.assertTrue(violations.getViolations().isEmpty());
    }

    private MockHttpServletResponse jsonResponse(int code, String json) throws UnsupportedEncodingException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().print(json);
        return response;
    }
}
