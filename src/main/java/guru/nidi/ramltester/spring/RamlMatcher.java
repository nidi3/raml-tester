package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlDefinition;
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
        final RamlViolations violations = ramlDefinition.testAgainst(result, servletUri);
        if (!violations.isEmpty()) {
            throw new AssertionError(violations.toString());
        }
    }
}
