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
