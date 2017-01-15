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
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v08.security.SecurityScheme;

import java.util.ArrayList;
import java.util.List;

public class SecScheme08 implements RamlSecScheme {
    private final SecurityScheme scheme;

    public SecScheme08(SecurityScheme scheme) {
        this.scheme = scheme;
    }

    static List<RamlSecScheme> of(List<SecurityScheme> schemes) {
        final List<RamlSecScheme> res = new ArrayList<>();
        for (final SecurityScheme s : schemes) {
            res.add(new SecScheme08(s));
        }
        return res;
    }

    @Override
    public String name() {
        return scheme.name();
    }

    @Override
    public String type() {
        return scheme.type();
    }

    @Override
    public String description() {
        return scheme.description().value();
    }

    @Override
    public RamlSecSchemePart describedBy() {
        return scheme.describedBy() == null ? null : new SecSchemePart08(scheme.describedBy());
    }

    @Override
    public RamlSecSchemeSettings settings() {
        return new SecSchemeSettings08(scheme.settings());
    }
}
