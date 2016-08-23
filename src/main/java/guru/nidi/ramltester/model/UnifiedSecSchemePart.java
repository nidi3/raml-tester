package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedSecSchemePart {
    List<UnifiedResponse> responses();

    List<UnifiedType> queryParameters();

    List<UnifiedType> headers();
}
