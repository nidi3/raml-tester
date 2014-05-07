package guru.nidi.ramltester;

/**
 *
 */
public class RamlTester {
    public RamlViolations test(RamlDefinition ramlDefinition, HttpRequest request, HttpResponse response) {
        final RamlTestRunner runner = new RamlTestRunner(ramlDefinition);
        runner.test(request, response);
        return runner.getViolations();
    }

}
