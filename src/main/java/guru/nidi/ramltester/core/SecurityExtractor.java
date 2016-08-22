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
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.security.SecurityScheme;
import org.raml.v2.api.model.v08.security.SecuritySchemePart;
import org.raml.v2.api.model.v08.security.SecuritySchemeRef;
import org.raml.v2.api.model.v08.security.SecuritySchemeSettings;
import org.raml.v2.api.model.v08.system.types.MarkdownString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
class SecurityExtractor {
    private final Api raml;
    private final List<SecurityScheme> schemes;

    public SecurityExtractor(Api raml, Method action, RamlViolations violations) {
        this.raml = raml;
        schemes = new SchemeFinder(raml, violations).securedBy(action);
    }

    public void check(RamlViolations violations) {
        for (final SecurityScheme scheme : raml.securitySchemes()) {
            final SecuritySchemeType type = SecuritySchemeType.of(scheme);
            if (type != null) {
                type.check(scheme, violations);
            }
        }
    }

    public List<SecurityScheme> getSchemes() {
        return schemes;
    }

    public List<Parameter> queryParameters(SecurityScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<Parameter>emptyList()
                : scheme.describedBy().queryParameters();
    }

    public List<Parameter> headers(SecurityScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<Parameter>emptyList()
                : scheme.describedBy().headers();
    }

    public List<Response> responses(SecurityScheme scheme) {
        return scheme.describedBy() == null
                ? Collections.<Response>emptyList()
                : scheme.describedBy().responses();
    }

    private static final class SchemeFinder {
        private static final SecurityScheme NULL_SCHEMA = new SecurityScheme() {

            @Override
            public MarkdownString description() {
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
            public SecuritySchemePart describedBy() {
                return null;
            }

            @Override
            public SecuritySchemeSettings settings() {
                return null;
            }
        };

        private final Api raml;
        private final RamlViolations violations;

        public SchemeFinder(Api raml, RamlViolations violations) {
            this.raml = raml;
            this.violations = violations;
        }

        public List<SecurityScheme> securedBy(Method action) {
            final List<SecurityScheme> res = new ArrayList<>();
            if (!action.securedBy().isEmpty()) {
                res.addAll(securitySchemes(action.securedBy(), new Message("securityScheme.undefined", new Locator(action))));
            } else if (!action.resource().securedBy().isEmpty()) {
                res.addAll(securitySchemes(action.resource().securedBy(), new Message("securityScheme.undefined", new Locator(action.resource()))));
            } else if (!raml.securedBy().isEmpty()) {
                res.addAll(securitySchemes(raml.securedBy(), new Message("securityScheme.undefined", new Locator())));
            }
            if (res.isEmpty()) {
                res.add(NULL_SCHEMA);
            }
            return res;
        }

        private List<SecurityScheme> securitySchemes(List<SecuritySchemeRef> refs, Message message) {
            final List<SecurityScheme> res = new ArrayList<>();
            for (final SecuritySchemeRef ref : refs) {
                res.add(ref == null ? NULL_SCHEMA : ref.securityScheme());
//                final String name = ref.name();
//                if ("null".equals(name)) {
//                    res.add(NULL_SCHEMA);
//                } else {
//
//                    final SecurityScheme ss = securityScheme(name);
//                    if (ss == null) {
//                        violations.add(message.withParam(name));
//                    } else {
//                        res.add(ss);
//                    }
//                }
            }
            return res;
        }

//        private SecurityScheme securityScheme(String name) {
//            for (final AbstractSecurityScheme scheme: raml.securitySchemes()) {
//                if (map.containsKey(name)) {
//                    return map.get(name);
//                }
//            }
//            return null;
//        }
    }
}
