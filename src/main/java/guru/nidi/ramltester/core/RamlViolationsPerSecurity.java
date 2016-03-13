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

import org.raml.model.SecurityScheme;

import java.util.*;

/**
 *
 */
class RamlViolationsPerSecurity {
    private static final Comparator<SecurityScheme> SCHEME_COMPARATOR = new Comparator<SecurityScheme>() {
        @Override
        public int compare(SecurityScheme s1, SecurityScheme s2) {
            return s1.getType().compareToIgnoreCase(s2.getType());
        }
    };

    private final List<SecurityScheme> schemes;
    private final Map<String, RamlViolations> requestViolations, responseViolations;

    public RamlViolationsPerSecurity(SecurityExtractor security) {
        schemes = new ArrayList<>(security.getSchemes());
        Collections.sort(schemes, SCHEME_COMPARATOR);
        requestViolations = new HashMap<>();
        responseViolations = new HashMap<>();
        for (final SecurityScheme scheme : schemes) {
            requestViolations.put(scheme.getType(), new RamlViolations());
            responseViolations.put(scheme.getType(), new RamlViolations());
        }
    }

    public RamlViolations requestViolations(SecurityScheme scheme) {
        return requestViolations.get(scheme.getType());
    }

    public RamlViolations responseViolations(SecurityScheme scheme) {
        return responseViolations.get(scheme.getType());
    }

    public List<SecurityScheme> leastViolations() {
        int best = Integer.MAX_VALUE;
        final List<SecurityScheme> res = new ArrayList<>();
        for (final SecurityScheme scheme : schemes) {
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
        for (final SecurityScheme scheme : leastViolations()) {
            addAll(scheme, requestViolations(scheme), request);
            addAll(scheme, responseViolations(scheme), response);
        }
    }

    private void addAll(SecurityScheme scheme, RamlViolations source, RamlViolations target) {
        if (schemes.size() == 1) {
            target.addAll(source);
        } else {
            for (final String s : source) {
                target.add("scheme", scheme.getType(), s);
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
