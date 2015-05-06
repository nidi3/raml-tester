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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MultiReportAggregator implements ReportAggregator {
    private final Map<String, List<RamlReport>> reports = new HashMap<>();

    @Override
    public RamlReport addReport(RamlReport report) {
        if (report != null) {
            final String title = report.getRaml().getTitle();
            final List<RamlReport> reportList = getOrCreateReports(title);
            reportList.add(report);
        }
        return report;
    }

    public List<RamlReport> getReports(RamlDefinition definition) {
        return getOrCreateReports(definition.getRaml().getTitle());
    }

    public Usage getUsage(RamlDefinition definition) {
        return UsageBuilder.usage(definition.getRaml(), getReports(definition));
    }

    public UsageProvider usageProvider(final RamlDefinition definition) {
        return new UsageProvider() {
            @Override
            public Usage getUsage() {
                return MultiReportAggregator.this.getUsage(definition);
            }
        };
    }

    public Iterable<Map.Entry<String, List<RamlReport>>> reports() {
        return reports.entrySet();
    }

    @Override
    public Iterable<Map.Entry<String, Usage>> usages() {
        final Map<String, Usage> res = new HashMap<>();
        for (final Map.Entry<String, List<RamlReport>> entry : reports.entrySet()) {
            res.put(entry.getKey(), UsageBuilder.usage(entry.getValue().get(0).getRaml(), entry.getValue()));
        }
        return res.entrySet();
    }

    @Override
    public void clear() {
        reports.clear();
    }

    private List<RamlReport> getOrCreateReports(String name) {
        List<RamlReport> reportList = reports.get(name);
        if (reportList == null) {
            reportList = new ArrayList<>();
            reports.put(name, reportList);
        }
        return reportList;
    }
}
