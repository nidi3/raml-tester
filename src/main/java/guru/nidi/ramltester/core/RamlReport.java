/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.internal.RamlApi;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.common.ValidationResult;

public class RamlReport {
    private final RamlApi raml;
    private final Usage usage = new Usage();
    private final RamlViolations requestViolations = new RamlViolations();
    private final RamlViolations responseViolations = new RamlViolations();
    private final RamlViolations validationViolations = new RamlViolations();

    public RamlReport(RamlApi raml) {
        this.raml = raml;
    }

    public static RamlReport fromModelResult(RamlApi raml, RamlModelResult modelResult) {
        final RamlReport report = new RamlReport(raml);
        for (final ValidationResult result : modelResult.getValidationResults()) {
            report.getValidationViolations().add("checking.exception", result.toString());
        }
        return report;
    }

    public boolean isEmpty() {
        return requestViolations.isEmpty() && responseViolations.isEmpty() && validationViolations.isEmpty();
    }

    @Override
    public String toString() {
        return "RamlReport{" +
                "requestViolations=" + requestViolations +
                ", responseViolations=" + responseViolations +
                ", validationViolations=" + validationViolations +
                '}';
    }

    Usage getUsage() {
        return usage;
    }

    public RamlApi getRaml() {
        return raml;
    }

    public RamlViolations getRequestViolations() {
        return requestViolations;
    }

    public RamlViolations getResponseViolations() {
        return responseViolations;
    }

    public RamlViolations getValidationViolations() {
        return validationViolations;
    }
}
