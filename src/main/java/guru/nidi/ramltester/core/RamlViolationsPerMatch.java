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

import guru.nidi.ramltester.model.internal.RamlMethod;

import java.util.*;

class RamlViolationsPerMatch {
    private List<ViolationInfo> infos = new ArrayList<>();

    public RamlViolations newViolations() {
        final RamlViolations v = new RamlViolations();
        infos.add(new ViolationInfo(v));
        return v;
    }

    public void setMethod(RamlMethod method) {
        infos.get(infos.size() - 1).method = method;
    }

    public RamlMethod bestMethod(RamlViolations addTo) {
        Collections.sort(infos);
        addTo.addAll(infos.get(0).violations);
        if (infos.isEmpty() || infos.get(0).method == null) {
            throw new RamlViolationException();
        }
        return infos.get(0).method;
    }

    private static class ViolationInfo implements Comparable<ViolationInfo> {
        private final RamlViolations violations;
        private RamlMethod method;

        public ViolationInfo(RamlViolations violations) {
            this.violations = violations;
        }

        @Override
        public int compareTo(ViolationInfo vi) {
            if (method != null && vi.method == null) {
                return -1;
            }
            if (method == null && vi.method != null) {
                return 1;
            }
            if (violations.size() == 1 && vi.violations.size() == 1) {
                final String msg = violations.asList().get(0).getMessage();
                final String otherMsg = vi.violations.asList().get(0).getMessage();
                return sortable(msg) - sortable(otherMsg);
            }
            return violations.size() - vi.violations.size();
        }

        private int sortable(String message) {
            if (message.startsWith("Action")) {
                return 1;
            }
            if (message.contains("by both")) {
                return 2;
            }
            return message.length();
        }
    }
}
