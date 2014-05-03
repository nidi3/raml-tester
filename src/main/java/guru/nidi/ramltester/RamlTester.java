package guru.nidi.ramltester;

import org.raml.model.Raml;

/**
 *
 */
public class RamlTester {
    private final SchemaValidator schemaValidator;

    public RamlTester() {
        this(new RestassuredSchemaValidator());
    }

    public RamlTester(SchemaValidator schemaValidator) {
        this.schemaValidator = schemaValidator;
    }

    public RamlViolations test(Raml raml, HttpRequest request, HttpResponse response) {
        final RamlTestRunner runner = new RamlTestRunner(raml, schemaValidator);
        runner.test(request, response);
        return runner.getViolations();
    }

}
