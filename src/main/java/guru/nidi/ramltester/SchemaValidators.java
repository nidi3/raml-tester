package guru.nidi.ramltester;

import guru.nidi.ramltester.core.JavaXmlSchemaValidator;
import guru.nidi.ramltester.core.RestassuredSchemaValidator;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.RamlResourceLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
class SchemaValidators {
    private final List<SchemaValidator> validators;

    private SchemaValidators(List<SchemaValidator> validators) {
        this.validators = validators;
    }

    public static SchemaValidators empty() {
        return new SchemaValidators(Collections.<SchemaValidator>emptyList());
    }

    public static SchemaValidators standard() {
        return new SchemaValidators(Arrays.asList(new RestassuredSchemaValidator(), new JavaXmlSchemaValidator()));
    }

    public SchemaValidators withSchemaValidator(SchemaValidator schemaValidator) {
        final ArrayList<SchemaValidator> newValidators = new ArrayList<>(validators);
        newValidators.add(schemaValidator);
        return new SchemaValidators(newValidators);
    }

    public SchemaValidators withResourceLoader(RamlResourceLoader resourceLoader) {
        final ArrayList<SchemaValidator> newValidators = new ArrayList<>();
        for (SchemaValidator validator : validators) {
            newValidators.add(validator.withResourceLoader(resourceLoader));
        }
        return new SchemaValidators(newValidators);
    }

    public List<SchemaValidator> getValidators() {
        return validators;
    }
}
