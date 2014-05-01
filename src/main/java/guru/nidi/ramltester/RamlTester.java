package guru.nidi.ramltester;

import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 */
public class RamlTester {
    private final MatcherProvider<String> schemaValidatorProvider;

    public RamlTester(MatcherProvider<String> schemaValidatorProvider) {
        this.schemaValidatorProvider = schemaValidatorProvider;
    }

    public RamlTester() {
        this(new RestassuredMatcherProvider());
    }

    public RamlViolations test(String raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        return test(new RamlDocumentBuilder().build(raml), request, response);
    }

    public RamlViolations test(Raml raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        final RamlTestRunner runner = new RamlTestRunner(raml, schemaValidatorProvider);
        runner.test(request, response);
        return runner.getViolations();
    }
}
