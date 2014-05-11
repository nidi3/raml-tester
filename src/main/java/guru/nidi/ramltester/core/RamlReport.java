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

/**
 *
 */
public class RamlReport {
    private final RamlViolations requestViolations = new RamlViolations();
    private final RamlViolations responseViolations = new RamlViolations();

    public RamlReport() {
    }

    public boolean isEmpty() {
        return requestViolations.isEmpty() && responseViolations.isEmpty();
    }

    @Override
    public String toString() {
        return "RamlReport{" +
                "requestViolations=" + requestViolations +
                ", responseViolations=" + responseViolations +
                '}';
    }

    public RamlViolations getRequestViolations() {
        return requestViolations;
    }

    public RamlViolations getResponseViolations() {
        return responseViolations;
    }
}
