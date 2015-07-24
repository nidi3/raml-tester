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

import org.raml.model.*;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
class SecurityExtractor {
    private final Raml raml;
    private final List<SecurityScheme> schemes;

    public SecurityExtractor(Raml raml, Action action, RamlViolations violations) {
        this.raml = raml;
        schemes = new SchemeFinder(raml, violations).securedBy(action);
    }

    public void check(RamlViolations violations) {
        for (final Map<String, SecurityScheme> schemeMap : raml.getSecuritySchemes()) {
            for (final SecurityScheme scheme : schemeMap.values()) {
                final SecuritySchemeType type = SecuritySchemeType.byName(scheme.getType());
                if (type != null) {
                    type.check(scheme, violations);
                }
            }
        }
    }

    private class RemovePropagatingList<T> extends ArrayList<T> {
        @Override
        public T remove(int index) {
            final T removed = super.remove(index);
            schemes.remove(index);
            return removed;
        }
    }

    public List<Map<String, QueryParameter>> queryParameters() {
        final List<Map<String, QueryParameter>> res = new RemovePropagatingList<>();
        for (final SecurityScheme scheme : schemes) {
            if (scheme.getDescribedBy() != null) {
                res.add(scheme.getDescribedBy().getQueryParameters());
            }
        }
        return res;
    }

    public List<Map<String, Header>> headers() {
        final List<Map<String, Header>> res = new RemovePropagatingList<>();
        for (final SecurityScheme scheme : schemes) {
            if (scheme.getDescribedBy() != null) {
                res.add(scheme.getDescribedBy().getHeaders());
            }
        }
        return res;
    }

    public List<Map<String, Response>> responses() {
        final List<Map<String, Response>> res = new RemovePropagatingList<>();
        for (final SecurityScheme scheme : schemes) {
            if (scheme.getDescribedBy() != null) {
                res.add(scheme.getDescribedBy().getResponses());
            }
        }
        return res;
    }

    private static final class SchemeFinder {
        private final Raml raml;
        private final RamlViolations violations;

        public SchemeFinder(Raml raml, RamlViolations violations) {
            this.raml = raml;
            this.violations = violations;
        }

        public List<SecurityScheme> securedBy(Action action) {
            final List<SecurityScheme> res = new ArrayList<>();
            if (!action.getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(action.getSecuredBy(), new Message("securityScheme.local.undefined", action)));
            } else if (!action.getResource().getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(action.getResource().getSecuredBy(), new Message("securityScheme.local.undefined", action.getResource())));
            } else if (!raml.getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(raml.getSecuredBy(), new Message("securityScheme.global.undefined")));
            }
            return res;
        }

        private List<SecurityScheme> securitySchemes(List<SecurityReference> refs, Message message) {
            final List<SecurityScheme> res = new ArrayList<>();
            for (final SecurityReference ref : refs) {
                final String name = ref.getName();
                final SecurityScheme ss = securityScheme(name);
                if (ss == null) {
                    violations.addIf(!name.equals("null"), message.withParam(name));
                } else {
                    res.add(ss);
                }
            }
            return res;
        }

        private SecurityScheme securityScheme(String name) {
            for (final Map<String, SecurityScheme> map : raml.getSecuritySchemes()) {
                if (map.containsKey(name)) {
                    return map.get(name);
                }
            }
            return null;
        }
    }
}
