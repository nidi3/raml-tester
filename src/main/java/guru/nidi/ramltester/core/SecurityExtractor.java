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

import guru.nidi.ramltester.model.internal.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SecurityExtractor {
    private final List<RamlSecScheme> schemes;

    public SecurityExtractor(RamlApi raml, RamlMethod action, RamlViolations violations) {
        schemes = new SchemeFinder(raml, violations).securedBy(action);
    }

    public List<RamlSecScheme> getSchemes() {
        return schemes;
    }

    public List<RamlType> queryParameters(RamlSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<RamlType>emptyList()
                : scheme.describedBy().queryParameters();
    }

    public List<RamlType> headers(RamlSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<RamlType>emptyList()
                : scheme.describedBy().headers();
    }

    public List<RamlApiResponse> responses(RamlSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<RamlApiResponse>emptyList()
                : scheme.describedBy().responses();
    }

    private static final class SchemeFinder {
        private static final RamlSecScheme NULL_SCHEMA = new RamlSecScheme() {

            @Override
            public String description() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String type() {
                return "null";
            }

            @Override
            public RamlSecSchemePart describedBy() {
                return null;
            }

            @Override
            public RamlSecSchemeSettings settings() {
                return null;
            }
        };

        private final RamlApi raml;
        private final RamlViolations violations;

        public SchemeFinder(RamlApi raml, RamlViolations violations) {
            this.raml = raml;
            this.violations = violations;
        }

        public List<RamlSecScheme> securedBy(RamlMethod action) {
            final List<RamlSecScheme> res = new ArrayList<>();
            if (!action.securedBy().isEmpty()) {
                res.addAll(securitySchemes(action.securedBy()));
            } else if (!action.resource().securedBy().isEmpty()) {
                res.addAll(securitySchemes(action.resource().securedBy()));
            } else if (!raml.securedBy().isEmpty()) {
                res.addAll(securitySchemes(raml.securedBy()));
            }
            if (res.isEmpty()) {
                res.add(NULL_SCHEMA);
            }
            return res;
        }

        private List<RamlSecScheme> securitySchemes(List<RamlSecSchemeRef> refs) {
            final List<RamlSecScheme> res = new ArrayList<>();
            for (final RamlSecSchemeRef ref : refs) {
                res.add(ref.securityScheme() == null ? NULL_SCHEMA : ref.securityScheme());
            }
            return res;
        }
    }
}
