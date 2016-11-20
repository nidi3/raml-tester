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
package guru.nidi.ramltester;

import guru.nidi.ramltester.core.*;
import org.raml.model.Raml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SimpleReportAggregator implements ReportAggregator, UsageProvider {
    private Raml raml;
    private final List<RamlReport> reports = new ArrayList<>();

    @Override
    public RamlReport addReport(RamlReport report) {
        if (report != null) {
            if (raml == null) {
                raml = report.getRaml();
            } else if (!raml.getTitle().equals(report.getRaml().getTitle())) {
                throw new IllegalArgumentException("This aggregator can only be used with one RamlDefinition. To work with multiple RamlDefinitions, use MultiReportAggregator.");
            }
            reports.add(report);
        }
        return report;
    }

    @Override
    public Iterable<Map.Entry<String, Usage>> usages() {
        return reports.isEmpty()
                ? Collections.<String, Usage>emptyMap().entrySet()
                : Collections.singletonMap(raml.getTitle(), UsageBuilder.usage(raml, reports)).entrySet();
    }

    @Override
    public void clear() {
        reports.clear();
    }

    public List<RamlReport> getReports() {
        return reports;
    }

    protected Raml getRaml() {
        return raml;
    }

    public Usage getUsage() {
        return UsageBuilder.usage(raml, getReports());
    }
}
