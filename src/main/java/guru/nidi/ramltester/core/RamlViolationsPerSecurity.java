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

import guru.nidi.ramltester.model.UnifiedSecScheme;

import java.util.*;

class RamlViolationsPerSecurity {
    private static final Comparator<UnifiedSecScheme> SCHEME_COMPARATOR = new Comparator<UnifiedSecScheme>() {
        @Override
        public int compare(UnifiedSecScheme s1, UnifiedSecScheme s2) {
            return s1.type().compareToIgnoreCase(s2.type());
        }
    };

    private final List<UnifiedSecScheme> schemes;
    private final Map<String, RamlViolations> requestViolations, responseViolations;

    public RamlViolationsPerSecurity(SecurityExtractor security) {
        schemes = new ArrayList<>(security.getSchemes());
        Collections.sort(schemes, SCHEME_COMPARATOR);
        requestViolations = new HashMap<>();
        responseViolations = new HashMap<>();
        for (final UnifiedSecScheme scheme : schemes) {
            requestViolations.put(scheme.type(), new RamlViolations());
            responseViolations.put(scheme.type(), new RamlViolations());
        }
    }

    public RamlViolations requestViolations(UnifiedSecScheme scheme) {
        return requestViolations.get(scheme.type());
    }

    public RamlViolations responseViolations(UnifiedSecScheme scheme) {
        return responseViolations.get(scheme.type());
    }

    public List<UnifiedSecScheme> leastViolations() {
        int best = Integer.MAX_VALUE;
        final List<UnifiedSecScheme> res = new ArrayList<>();
        for (final UnifiedSecScheme scheme : schemes) {
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
        for (final UnifiedSecScheme scheme : leastViolations()) {
            addAll(scheme, requestViolations(scheme), request);
            addAll(scheme, responseViolations(scheme), response);
        }
    }

    private void addAll(UnifiedSecScheme scheme, RamlViolations source, RamlViolations target) {
        if (schemes.size() == 1) {
            target.addAll(source);
        } else {
            for (final RamlViolationMessage s : source) {
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
