package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlTester;
import guru.nidi.ramltester.RamlViolations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
*
*/
public class RamlMatcher implements ResultMatcher {
    private final RamlDefinition ramlDefinition;
    private final String servletUri;

    public RamlMatcher(RamlDefinition ramlDefinition, String servletUri) {
        this.ramlDefinition = ramlDefinition;
        this.servletUri = servletUri;
    }

    @Override
    public void match(MvcResult result) throws Exception {
        final RamlViolations violations = new RamlTester().test(ramlDefinition,
                new SpringMockHttpRequest(servletUri, result.getRequest()),
                new SpringMockHttpResponse(result.getResponse()));
        if (!violations.getViolations().isEmpty()) {
            throw new AssertionError(violations.toString());
        }
    }
}
