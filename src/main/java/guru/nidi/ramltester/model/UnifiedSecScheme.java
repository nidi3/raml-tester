package guru.nidi.ramltester.model;

/**
 *
 */
public interface UnifiedSecScheme {
    String name();

    String type();

    String description();

    UnifiedSecSchemePart describedBy();

    UnifiedSecSchemeSettings settings();
}
