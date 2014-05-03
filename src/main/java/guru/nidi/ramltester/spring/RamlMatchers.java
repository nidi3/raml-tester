package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlTester;
import guru.nidi.ramltester.RamlViolations;
import guru.nidi.ramltester.SchemaValidator;
import org.raml.model.Raml;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 *
 */
public class RamlMatchers {
    public static RamlMatcher matchesRaml(Raml raml) {
        return new RamlMatcher(raml);
    }
    public static RamlMatcher matchesRaml(Raml raml, SchemaValidator schemaValidator) {
        return new RamlMatcher(raml, schemaValidator);
    }

    public static class RamlMatcher implements ResultMatcher {
        private final Raml raml;
        private final RamlTester tester;

        public RamlMatcher(Raml raml) {
            this.raml = raml;
            this.tester = new RamlTester();
        }

        public RamlMatcher(Raml raml, SchemaValidator schemaMatcher) {
            this.raml = raml;
            this.tester = new RamlTester(schemaMatcher);
        }

        @Override
        public void match(MvcResult result) throws Exception {
            final RamlViolations violations = tester.test(raml, new SpringMockHttpRequest(result.getRequest()), new SpringMockHttpResponse(result.getResponse()));
            if (!violations.getViolations().isEmpty()) {
                throw new AssertionError(violations.toString());
            }
        }
    }
}
