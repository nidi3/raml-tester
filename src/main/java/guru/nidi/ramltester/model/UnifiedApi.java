package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedApi {
    String title();

    String version();

    String baseUri();

    List<UnifiedDocItem> documentation();

    List<String> protocols();

    String ramlVersion();

    List<UnifiedResource> resources();

    List<UnifiedType> baseUriParameters();

    List<UnifiedSecScheme> securitySchemes();

    List<UnifiedSecSchemeRef> securedBy();
}
