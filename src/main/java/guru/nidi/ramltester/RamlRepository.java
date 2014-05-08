package guru.nidi.ramltester;

/**
 *
 */
public interface RamlRepository {
    RamlDefinition getRaml(String name);

    Iterable<String> getNames();

    RamlRepository withSchemaValidator(SchemaValidator schemaValidator);
}

