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

import guru.nidi.ramltester.util.Message;
import org.raml.model.*;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;

import java.util.ArrayList;
import java.util.Collections;
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

    public List<SecurityScheme> getSchemes() {
        return schemes;
    }

    public Map<String, QueryParameter> queryParameters(SecurityScheme scheme) {
        return scheme.getDescribedBy() == null
                ? Collections.<String, QueryParameter>emptyMap()
                : scheme.getDescribedBy().getQueryParameters();
    }

    public Map<String, Header> headers(SecurityScheme scheme) {
        return scheme.getDescribedBy() == null
                ? Collections.<String, Header>emptyMap()
                : scheme.getDescribedBy().getHeaders();
    }

    public Map<String, Response> responses(SecurityScheme scheme) {
        return scheme.getDescribedBy() == null
                ? Collections.<String, Response>emptyMap()
                : scheme.getDescribedBy().getResponses();
    }

    private static final class SchemeFinder {
        private static final SecurityScheme NULL_SCHEMA = new SecurityScheme();

        static {
            NULL_SCHEMA.setType("null");
        }

        private final Raml raml;
        private final RamlViolations violations;

        public SchemeFinder(Raml raml, RamlViolations violations) {
            this.raml = raml;
            this.violations = violations;
        }

        public List<SecurityScheme> securedBy(Action action) {
            final List<SecurityScheme> res = new ArrayList<>();
            if (!action.getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(action.getSecuredBy(), new Message("securityScheme.undefined", new Locator(action))));
            } else if (!action.getResource().getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(action.getResource().getSecuredBy(), new Message("securityScheme.undefined", new Locator(action.getResource()))));
            } else if (!raml.getSecuredBy().isEmpty()) {
                res.addAll(securitySchemes(raml.getSecuredBy(), new Message("securityScheme.undefined", new Locator())));
            }
            if (res.isEmpty()) {
                res.add(NULL_SCHEMA);
            }
            return res;
        }

        private List<SecurityScheme> securitySchemes(List<SecurityReference> refs, Message message) {
            final List<SecurityScheme> res = new ArrayList<>();
            for (final SecurityReference ref : refs) {
                final String name = ref.getName();
                if ("null".equals(name)) {
                    res.add(NULL_SCHEMA);
                } else {
                    final SecurityScheme ss = securityScheme(name);
                    if (ss == null) {
                        violations.add(message.withParam(name));
                    } else {
                        res.add(ss);
                    }
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
