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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.DummyReportAggragator;
import guru.nidi.ramltester.core.RamlChecker;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.ReportAggregator;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

public class RamlMatcher implements ResultMatcher {
    private final RamlChecker checker;
    private final ReportAggregator aggregator;

    private RamlMatcher(RamlChecker checker, ReportAggregator aggregator) {
        this.checker = checker;
        this.aggregator = aggregator;
    }

    public RamlMatcher(RamlChecker checker) {
        this(checker, new DummyReportAggragator());
    }

    public RamlMatcher aggregating(ReportAggregator aggregator) {
        return new RamlMatcher(checker, aggregator);
    }

    @Override
    public void match(MvcResult result) {
        final RamlReport report = testAgainst(result);
        if (!report.isEmpty()) {
            throw new AssertionError(report.toString());
        }
    }

    public RamlReport testAgainst(MvcResult result) {
        return aggregator.addReport(checker.check(
                new SpringMockRamlRequest(result.getRequest()),
                new SpringMockRamlResponse(result.getResponse())));
    }
}
