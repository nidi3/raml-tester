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

import guru.nidi.ramltester.core.RamlCoverage;
import guru.nidi.ramltester.core.RamlReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MultiReportAggregator implements ReportAggregator {
    private Map<String, List<RamlReport>> reports = new HashMap<>();

    @Override
    public RamlReport addReport(RamlReport report) {
        final String title = report.getRaml().getTitle();
        final List<RamlReport> reportList = getOrCreateReports(title);
        reportList.add(report);
        return report;
    }

    public List<RamlReport> getReports(RamlDefinition definition) {
        return getOrCreateReports(definition.getRaml().getTitle());
    }

    public RamlCoverage getCoverage(RamlDefinition definition) {
        return new RamlCoverage(definition.getRaml(), getReports(definition));
    }

    public CoverageProvider coverageProvider(final RamlDefinition definition) {
        return new CoverageProvider() {
            @Override
            public RamlCoverage getCoverage() {
                return MultiReportAggregator.this.getCoverage(definition);
            }
        };
    }

    public Iterable<Map.Entry<String, List<RamlReport>>> reports() {
        return reports.entrySet();
    }

    public Iterable<Map.Entry<String, RamlCoverage>> coverages() {
        Map<String, RamlCoverage> res = new HashMap<>();
        for (Map.Entry<String, List<RamlReport>> entry : reports.entrySet()) {
            res.put(entry.getKey(), new RamlCoverage(entry.getValue().get(0).getRaml(), entry.getValue()));
        }
        return res.entrySet();
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
