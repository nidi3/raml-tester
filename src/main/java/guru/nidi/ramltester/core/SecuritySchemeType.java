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
package guru.nidi.ramltester.core;

import org.raml.model.SecurityScheme;
import org.raml.model.SecuritySettings;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
enum SecuritySchemeType {
    OAUTH_10("OAuth 1.0") {
        @Override
        public void check(SecurityScheme scheme, RamlViolations violations) {
            final SecuritySettings settings = scheme.getSettings();
            violations.addIf(settings == null || settings.getRequestTokenUri() == null, "oauth10.requestTokenUri.missing");
            violations.addIf(settings == null || settings.getAuthorizationUri() == null, "oauth10.authorizationUri.missing");
            violations.addIf(settings == null || settings.getTokenCredentialsUri() == null, "oauth10.tokenCredentialsUri.missing");
        }
    },
    OAUTH_20("OAuth 2.0") {
        private final List<String> GRANTS = Arrays.asList("code", "token", "owner", "credentials");

        @Override
        public void check(SecurityScheme scheme, RamlViolations violations) {
            final SecuritySettings settings = scheme.getSettings();
            violations.addIf(settings == null || settings.getAuthorizationUri() == null, "oauth20.authorizationUri.missing");
            violations.addIf(settings == null || settings.getAccessTokenUri() == null, "oauth20.accessTokenUri.missing");
            violations.addIf(settings == null || settings.getAuthorizationGrants().isEmpty(), "oauth20.authorizationGrants.missing");
            if (settings != null) {
                for (final String grant : settings.getAuthorizationGrants()) {
                    violations.addIf(!GRANTS.contains(grant), "oauth20.authorizationGrant.invalid", grant);
                }
            }
        }
    },
    BASIC("Basic Authentication") {
        @Override
        public void check(SecurityScheme scheme, RamlViolations violations) {

        }
    },
    DIGEST("Digest Authentication") {
        @Override
        public void check(SecurityScheme scheme, RamlViolations violations) {

        }
    };

    private final String name;

    SecuritySchemeType(String name) {
        this.name = name;
    }

    abstract public void check(SecurityScheme scheme, RamlViolations violations);

    public static SecuritySchemeType byName(String name) {
        for (final SecuritySchemeType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
