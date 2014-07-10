package guru.nidi.ramltester;

import guru.nidi.ramltester.core.RamlCoverage;
import guru.nidi.ramltester.core.RamlReport;
import org.raml.model.Raml;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimpleReportAggregator implements ReportAggregator, CoverageProvider {
    private Raml raml;
    private List<RamlReport> reports = new ArrayList<>();

    @Override
    public RamlReport addReport(RamlReport report) {
        if (raml == null) {
            raml = report.getRaml();
        } else if (!raml.getTitle().equals(report.getRaml().getTitle())) {
            throw new IllegalArgumentException("This aggregator can only be used with one RamlDefinition. To work with multiple RamlDefinitions, use MultiReportAggregator.");
        }
        reports.add(report);
        return report;
    }

    public List<RamlReport> getReports() {
        return reports;
    }

    public RamlCoverage getCoverage() {
        return new RamlCoverage(raml, getReports());
    }
}
