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
