package guru.nidi.ramltester;

import org.raml.model.Raml;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

/**
 *
 */
public class RamlDefinition {
    private final Raml raml;
    private final SchemaValidator schemaValidator;

    private RamlDefinition(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public static RamlDefinition fromClasspath(String name) {
        return new RamlDefinition(new RamlDocumentBuilder(new ClassPathResourceLoader()).build(name), null);
    }

    public static RamlDefinition fromClasspath(Class<?> basePackage, String name) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/') + "/" + name);
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
