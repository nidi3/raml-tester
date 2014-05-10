package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlTester;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 *
 */
public class RamlMatcher implements ResultMatcher {
    private final RamlTester tester;
    private final String servletUri;

    public RamlMatcher(RamlTester tester, String servletUri) {
        this.tester = tester;
        this.servletUri = servletUri;
    }

    public RamlMatcher assumingServletUri(String servletUri) {
        return new RamlMatcher(tester, servletUri);
    }

    @Override
    public void match(MvcResult result) throws Exception {
        final RamlReport report = testAgainst(result);
        if (!report.isEmpty()) {
            throw new AssertionError(report.toString());
        }
    }

    public RamlReport testAgainst(MvcResult result) {
        return tester.test(
                new SpringMockRamlRequest(servletUri, result.getRequest()),
                new SpringMockRamlResponse(result.getResponse()));
    }
}
