package guru.nidi.ramltester;

import guru.nidi.ramltester.core.RamlReport;

/**
 *
 */
public interface ReportAggregator {
    RamlReport addReport(RamlReport report);
}
