package guru.nidi.ramltester;

/**
 *
 */
public interface SchemaValidator {
    void validate(RamlViolations violations,String content,String schema);
}
