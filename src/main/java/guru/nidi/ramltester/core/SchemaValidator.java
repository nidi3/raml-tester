package guru.nidi.ramltester.core;

/**
 *
 */
public interface SchemaValidator {
    void validate(String content, String schema, RamlViolations violations, Message message);
}
