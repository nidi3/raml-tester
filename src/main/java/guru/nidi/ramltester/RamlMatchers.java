package guru.nidi.ramltester;

import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 *
 */
public class RamlMatchers {
    public static RamlMatcher matchesRaml(String raml) {
        return new RamlMatcher(raml, new RestassuredMatcherProvider());
    }

    public static RamlMatcher matchesRaml(String raml, MatcherProvider<String> schemaMatcher) {
        return new RamlMatcher(raml, schemaMatcher);
    }

    public static class RamlMatcher implements ResultMatcher {
        private final Raml raml;
        private final RamlTester tester;

        public RamlMatcher(String raml, MatcherProvider<String> schemaMatcher) {
            this.raml = new RamlDocumentBuilder().build(raml);
            this.tester = new RamlTester(schemaMatcher);
        }


        @Override
        public void match(MvcResult result) throws Exception {
            final RamlViolations violations = tester.test(raml, result.getRequest(), result.getResponse());
            if (!violations.getViolations().isEmpty()) {
                throw new AssertionError(violations.toString());
            }
        }
    }
}
