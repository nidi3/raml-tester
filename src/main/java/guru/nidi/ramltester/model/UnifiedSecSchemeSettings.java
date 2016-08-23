package guru.nidi.ramltester.model;

import java.util.List;

/**
 *
 */
public interface UnifiedSecSchemeSettings {
    String requestTokenUri();

    String authorizationUri();

    String tokenCredentialsUri();

    List<String> signatures();

    String accessTokenUri();

    List<String> authorizationGrants();

    List<String> scopes();
}
