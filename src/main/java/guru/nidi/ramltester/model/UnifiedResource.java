package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedResource {
    String description();

    String displayName();

    String relativeUri();

    List<UnifiedResource> resources();

    String resourcePath();

    UnifiedResource parentResource();

    List<UnifiedType> baseUriParameters();

    List<UnifiedMethod> methods();

    List<UnifiedType> uriParameters();

    List<UnifiedSecSchemeRef> securedBy();
}
