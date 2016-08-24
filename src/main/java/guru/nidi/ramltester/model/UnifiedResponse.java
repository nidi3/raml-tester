package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedResponse {
    String description();

    String code();

    List<UnifiedType> headers();

    List<UnifiedBody> body();
}
