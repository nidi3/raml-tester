package guru.nidi.ramltester.core;

import guru.nidi.ramltester.loader.RamlResourceLoader;

/**
 *
 */
public interface SchemaValidator {
    SchemaValidator withResourceLoader(String base, RamlResourceLoader resourceLoader);

    void validate(String content, String schema, RamlViolations violations, Message message);
}
