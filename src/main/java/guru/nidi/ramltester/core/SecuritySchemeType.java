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

import guru.nidi.ramltester.model.UnifiedSecScheme;
import guru.nidi.ramltester.model.UnifiedSecSchemeSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
abstract class SecuritySchemeType {
    private static final Map<String, SecuritySchemeType> INSTANCES = new HashMap<>();

    public static SecuritySchemeType of(UnifiedSecScheme scheme) {
        return INSTANCES.get(scheme.type());
    }

    public abstract void check(UnifiedSecScheme scheme, RamlViolations violations);

    static {
        INSTANCES.put("oauth_2_0", new SecuritySchemeType() {
            @Override
            public void check(UnifiedSecScheme scheme, RamlViolations violations) {
                final UnifiedSecSchemeSettings settings = scheme.settings();
                violations.addIf(settings == null || settings.requestTokenUri() == null, "oauth10.requestTokenUri.missing");
                violations.addIf(settings == null || settings.authorizationUri() == null, "oauth10.authorizationUri.missing");
                violations.addIf(settings == null || settings.tokenCredentialsUri() == null, "oauth10.tokenCredentialsUri.missing");
            }
        });
        INSTANCES.put("oauth_1_0", new SecuritySchemeType() {
            private final List<String> GRANTS = Arrays.asList("code", "token", "owner", "credentials");

            @Override
            public void check(UnifiedSecScheme scheme, RamlViolations violations) {
                final UnifiedSecSchemeSettings settings = scheme.settings();
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
        INSTANCES.put("basic", new SecuritySchemeType() {
            public void check(UnifiedSecScheme scheme, RamlViolations violations) {

            }
        });
        INSTANCES.put("digest", new SecuritySchemeType() {
            public void check(UnifiedSecScheme scheme, RamlViolations violations) {

            }
        });
    }
}
