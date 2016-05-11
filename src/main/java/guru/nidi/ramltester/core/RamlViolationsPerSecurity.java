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

import org.raml.v2.api.model.v08.security.AbstractSecurityScheme;

import java.util.*;

/**
 *
 */
class RamlViolationsPerSecurity {
    private static final Comparator<AbstractSecurityScheme> SCHEME_COMPARATOR = new Comparator<AbstractSecurityScheme>() {
        @Override
        public int compare(AbstractSecurityScheme s1, AbstractSecurityScheme s2) {
            return s1.type().compareToIgnoreCase(s2.type());
        }
    };

    private final List<AbstractSecurityScheme> schemes;
    private final Map<String, RamlViolations> requestViolations, responseViolations;

    public RamlViolationsPerSecurity(SecurityExtractor security) {
        schemes = new ArrayList<>(security.getSchemes());
        Collections.sort(schemes, SCHEME_COMPARATOR);
        requestViolations = new HashMap<>();
        responseViolations = new HashMap<>();
        for (final AbstractSecurityScheme scheme : schemes) {
            requestViolations.put(scheme.type(), new RamlViolations());
            responseViolations.put(scheme.type(), new RamlViolations());
        }
    }

    public RamlViolations requestViolations(AbstractSecurityScheme scheme) {
        return requestViolations.get(scheme.type());
    }

    public RamlViolations responseViolations(AbstractSecurityScheme scheme) {
        return responseViolations.get(scheme.type());
    }

    public List<AbstractSecurityScheme> leastViolations() {
        int best = Integer.MAX_VALUE;
        final List<AbstractSecurityScheme> res = new ArrayList<>();
        for (final AbstractSecurityScheme scheme : schemes) {
            final int violations = requestViolations(scheme).size() + responseViolations(scheme).size();
            if (violations <= best) {
                if (violations < best) {
                    res.clear();
                }
                res.add(scheme);
                best = violations;
            }
        }
        return res;
    }

    public void addLeastViolations(RamlViolations request, RamlViolations response) {
        for (final AbstractSecurityScheme scheme : leastViolations()) {
            addAll(scheme, requestViolations(scheme), request);
            addAll(scheme, responseViolations(scheme), response);
        }
    }

    private void addAll(AbstractSecurityScheme scheme, RamlViolations source, RamlViolations target) {
        if (schemes.size() == 1) {
            target.addAll(source);
        } else {
            for (final String s : source) {
                target.add("scheme", scheme.type(), s);
            }
        }
    }

    @Override
    public String toString() {
        return "RamlViolationsPerSecurity{" +
                "requestViolations=" + requestViolations +
                ", responseViolations=" + responseViolations +
                '}';
    }
}
