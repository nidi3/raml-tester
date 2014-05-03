package guru.nidi.ramltester;

import guru.nidi.ramltester.spring.SpringMockHttpRequest;
import guru.nidi.ramltester.spring.SpringMockHttpResponse;
import org.hamcrest.Matcher;
import org.hamcrest.core.*;
import org.raml.model.Raml;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestBase {
    protected MockHttpServletRequest get(String url) {
        return MockMvcRequestBuilders.get(url).buildRequest(new MockServletContext());
    }

    protected MockHttpServletRequest post(String url) {
        return MockMvcRequestBuilders.post(url).buildRequest(new MockServletContext());
    }

    protected Matcher<String> startsWith(String s) {
        return new StringStartsWith(s);
    }

    protected Matcher<String> contains(String s) {
        return new StringContains(s);
    }

    protected Matcher<String> endsWith(String s) {
        return new StringEndsWith(s);
    }

    @SafeVarargs
    protected final <T> Matcher<T> allOf(Matcher<? super T>... matcher) {
        return new AllOf<>(Arrays.asList(matcher));
    }

    protected MockHttpServletResponse jsonResponse(int code, String json, String contentType) throws UnsupportedEncodingException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(code);
        response.setContentType(contentType);
        response.getWriter().print(json);
        return response;
    }

    protected MockHttpServletResponse jsonResponse(int code, String json) throws UnsupportedEncodingException {
        return jsonResponse(code, json, "application/json");
    }

    protected void assertNoViolation(Raml raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        final RamlViolations violations = new RamlTester(new RestassuredSchemaValidator()).test(raml, new SpringMockHttpRequest(request), new SpringMockHttpResponse(response));
        assertTrue("Expected no violations, but found: " + violations, violations.getViolations().isEmpty());
    }

    protected void assertOneViolationThat(Raml raml, MockHttpServletRequest request, MockHttpServletResponse response, Matcher<String> matcher) {
        final RamlViolations violations = new RamlTester(new RestassuredSchemaValidator()).test(raml, new SpringMockHttpRequest(request), new SpringMockHttpResponse(response));
        assertThat("Expected one violation, but none found", 1, new IsEqual<>(violations.getViolations().size()));
        assertThat(violations.getViolations().get(0), matcher);
    }
}
