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

import guru.nidi.ramltester.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
class SecurityExtractor {
    private final UnifiedApi raml;
    private final List<UnifiedSecScheme> schemes;

    public SecurityExtractor(UnifiedApi raml, UnifiedMethod action, RamlViolations violations) {
        this.raml = raml;
        schemes = new SchemeFinder(raml, violations).securedBy(action);
    }

    public void check(RamlViolations violations) {
        for (final UnifiedSecScheme scheme : raml.securitySchemes()) {
            final SecuritySchemeType type = SecuritySchemeType.of(scheme);
            if (type != null) {
                type.check(scheme, violations);
            }
        }
    }

    public List<UnifiedSecScheme> getSchemes() {
        return schemes;
    }

    public List<UnifiedType> queryParameters(UnifiedSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<UnifiedType>emptyList()
                : scheme.describedBy().queryParameters();
    }

    public List<UnifiedType> headers(UnifiedSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<UnifiedType>emptyList()
                : scheme.describedBy().headers();
    }

    public List<UnifiedResponse> responses(UnifiedSecScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<UnifiedResponse>emptyList()
                : scheme.describedBy().responses();
    }

    private static final class SchemeFinder {
        private static final UnifiedSecScheme NULL_SCHEMA = new UnifiedSecScheme() {

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
            public UnifiedSecSchemePart describedBy() {
                return null;
            }

            @Override
            public UnifiedSecSchemeSettings settings() {
                return null;
            }
        };

        private final UnifiedApi raml;
        private final RamlViolations violations;

        public SchemeFinder(UnifiedApi raml, RamlViolations violations) {
            this.raml = raml;
            this.violations = violations;
        }

        public List<UnifiedSecScheme> securedBy(UnifiedMethod action) {
            final List<UnifiedSecScheme> res = new ArrayList<>();
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

        private List<UnifiedSecScheme> securitySchemes(List<UnifiedSecSchemeRef> refs) {
            final List<UnifiedSecScheme> res = new ArrayList<>();
            for (final UnifiedSecSchemeRef ref : refs) {
                res.add(ref.securityScheme() == null ? NULL_SCHEMA : ref.securityScheme());
            }
            return res;
        }
    }
}
