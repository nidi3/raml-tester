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

import org.raml.v2.api.model.v10.security.SecuritySchemePart;

import java.util.Collections;
import java.util.List;

class SecSchemePart10 implements RamlSecSchemePart {
    private final SecuritySchemePart part;

    public SecSchemePart10(SecuritySchemePart part) {
        this.part = part;
    }

    @Override
    public List<RamlApiResponse> responses() {
        return Response10.of(part.responses());
    }

    @Override
    public List<RamlType> queryParameters() {
        return Type10.of(part.queryString() == null ? part.queryParameters() : Collections.singletonList(part.queryString()));
    }

    @Override
    public List<RamlType> headers() {
        return Type10.of(part.headers());
    }
}
