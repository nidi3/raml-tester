package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedMethod {
    String description();

    String method();

    UnifiedResource resource();

    List<UnifiedType> baseUriParameters();

    List<String> protocols();

    List<UnifiedType> queryParameters();

    List<UnifiedType> headers();

    List<UnifiedResponse> responses();

    List<UnifiedSecSchemeRef> securedBy();

    List<UnifiedBody> body();
}
