package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedBody {
    String name();

    List<UnifiedType> formParameters();

    String type();

    List<String> examples();
}
