package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedType {
    <T> T delegate();

    String name();

    String description();

    List<String> examples();

    String defaultValue();
}
