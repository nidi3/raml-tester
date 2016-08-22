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

import org.raml.v2.api.model.v08.security.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
abstract class SecuritySchemeType<T extends SecurityScheme> {
    private static final Map<Class<?>, SecuritySchemeType<?>> INSTANCES = new HashMap<>();

    public static SecuritySchemeType of(SecurityScheme scheme) {
        return INSTANCES.get(scheme.getClass());
    }

    public abstract void check(T scheme, RamlViolations violations);

    static {
        INSTANCES.put(OAuth1SecurityScheme.class, new SecuritySchemeType<OAuth1SecurityScheme>() {
            @Override
            public void check(OAuth1SecurityScheme scheme, RamlViolations violations) {
                final OAuth1SecuritySchemeSettings settings = scheme.settings();
                violations.addIf(settings == null || settings.requestTokenUri() == null, "oauth10.requestTokenUri.missing");
                violations.addIf(settings == null || settings.authorizationUri() == null, "oauth10.authorizationUri.missing");
                violations.addIf(settings == null || settings.tokenCredentialsUri() == null, "oauth10.tokenCredentialsUri.missing");
            }
        });
        INSTANCES.put(OAuth2SecurityScheme.class, new SecuritySchemeType<OAuth2SecurityScheme>() {
            private final List<String> GRANTS = Arrays.asList("code", "token", "owner", "credentials");

            @Override
            public void check(OAuth2SecurityScheme scheme, RamlViolations violations) {
                final OAuth2SecuritySchemeSettings settings = scheme.settings();
                violations.addIf(settings == null || settings.authorizationUri() == null, "oauth20.authorizationUri.missing");
                violations.addIf(settings == null || settings.accessTokenUri() == null, "oauth20.accessTokenUri.missing");
                violations.addIf(settings == null || settings.authorizationGrants().isEmpty(), "oauth20.authorizationGrants.missing");
                if (settings != null) {
                    for (final String grant : settings.authorizationGrants()) {
                        violations.addIf(!GRANTS.contains(grant), "oauth20.authorizationGrant.invalid", grant);
                    }
                }
            }
        });
        INSTANCES.put(BasicSecurityScheme.class, new SecuritySchemeType<BasicSecurityScheme>() {
            public void check(BasicSecurityScheme scheme, RamlViolations violations) {

            }
        });
        INSTANCES.put(DigestSecurityScheme.class, new SecuritySchemeType<DigestSecurityScheme>() {
            public void check(DigestSecurityScheme scheme, RamlViolations violations) {

            }
        });
    }
}
