/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
