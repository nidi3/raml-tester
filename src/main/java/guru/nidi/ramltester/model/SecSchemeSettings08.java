package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.security.OAuth1SecuritySchemeSettings;
import org.raml.v2.api.model.v08.security.OAuth2SecuritySchemeSettings;
import org.raml.v2.api.model.v08.security.SecuritySchemeSettings;

import java.util.List;

/**
 *
 */
public class SecSchemeSettings08 implements UnifiedSecSchemeSettings {
    private SecuritySchemeSettings settings;

    public SecSchemeSettings08(SecuritySchemeSettings settings) {
        this.settings = settings;
    }

    @Override
    public String requestTokenUri() {
        return settings instanceof OAuth1SecuritySchemeSettings
                ? ((OAuth1SecuritySchemeSettings) settings).requestTokenUri().value()
                : null;
    }

    @Override
    public String authorizationUri() {
        return settings instanceof OAuth1SecuritySchemeSettings
                ? ((OAuth1SecuritySchemeSettings) settings).authorizationUri().value()
                : null;
    }

    @Override
    public String tokenCredentialsUri() {
        return settings instanceof OAuth1SecuritySchemeSettings
                ? ((OAuth1SecuritySchemeSettings) settings).tokenCredentialsUri().value()
                : null;
    }

    @Override
    public List<String> signatures() {
        return null;
    }

    @Override
    public String accessTokenUri() {
        return settings instanceof OAuth2SecuritySchemeSettings
                ? ((OAuth2SecuritySchemeSettings) settings).accessTokenUri().value()
                : null;
    }

    @Override
    public List<String> authorizationGrants() {
        return settings instanceof OAuth2SecuritySchemeSettings
                ? ((OAuth2SecuritySchemeSettings) settings).authorizationGrants()
                : null;
    }

    @Override
    public List<String> scopes() {
        return settings instanceof OAuth2SecuritySchemeSettings
                ? ((OAuth2SecuritySchemeSettings) settings).scopes()
                : null;
    }
}
