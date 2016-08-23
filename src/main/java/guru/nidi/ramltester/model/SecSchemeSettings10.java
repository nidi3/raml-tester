package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.security.SecuritySchemeSettings;

import java.util.List;

/**
 *
 */
public class SecSchemeSettings10 implements UnifiedSecSchemeSettings {
    private SecuritySchemeSettings settings;

    public SecSchemeSettings10(SecuritySchemeSettings settings) {
        this.settings = settings;
    }

    @Override
    public String requestTokenUri() {
        return settings.requestTokenUri().value();
    }

    @Override
    public String authorizationUri() {
        return settings.authorizationUri().value();
    }

    @Override
    public String tokenCredentialsUri() {
        return settings.tokenCredentialsUri().value();
    }

    @Override
    public List<String> signatures() {
        return settings.signatures();
    }

    @Override
    public String accessTokenUri() {
        return settings.accessTokenUri().value();
    }

    @Override
    public List<String> authorizationGrants() {
        return settings.authorizationGrants();
    }

    @Override
    public List<String> scopes() {
        return settings.scopes();
    }
}
