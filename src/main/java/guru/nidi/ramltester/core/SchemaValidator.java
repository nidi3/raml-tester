package guru.nidi.ramltester.core;

/**
 *
 */
public interface SchemaValidator {
    void validate(RamlViolations violations,String content,String schema);
}
