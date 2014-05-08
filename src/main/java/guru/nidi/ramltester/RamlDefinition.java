package guru.nidi.ramltester;

import org.raml.model.Raml;

/**
 *
 */
public class RamlDefinition {
    private final Raml raml;
    private final SchemaValidator schemaValidator;

    public RamlDefinition(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public RamlDefinition withSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlDefinition(raml, schemaValidator);
    }

    public Raml getRaml() {
        return raml;
    }

    public SchemaValidator getSchemaValidator() {
        return schemaValidator;
    }
}

