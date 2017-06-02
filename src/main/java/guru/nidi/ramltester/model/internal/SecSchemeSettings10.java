/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v10.security.SecuritySchemeSettings;

import java.util.List;

class SecSchemeSettings10 implements RamlSecSchemeSettings {
    private final SecuritySchemeSettings settings;

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
