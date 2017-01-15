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
package guru.nidi.ramltester.junit;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.util.Message;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.*;

public class MatcherTest {
    private final RamlReport reportNix = report(null, null, null);
    private final RamlReport reportVal = report("bla\nblu", null, null);
    private final RamlReport reportReq = report(null, "bla", null);
    private final RamlReport reportRes = report(null, null, "bla");

    @Test
    public void allOk() {
        final Matcher<RamlReport> matcher = RamlMatchers.hasNoViolations();
        assertMatch(matcher, reportNix);
    }

    @Test
    public void allNok() {
        final Matcher<RamlReport> matcher = RamlMatchers.hasNoViolations();
        assertNoMatch(matcher, reportVal, "\nValidation violations:\n  - bla\n    blu\n  - bla\n    blu");
        assertNoMatch(matcher, reportReq, "\nRequest violations:\n  - bla");
        assertNoMatch(matcher, reportRes, "\nResponse violations:\n  - bla");
    }

    @Test
    public void validation() {
        final Matcher<RamlReport> matcher = RamlMatchers.validates();
        assertNoMatch(matcher, reportVal, "\nValidation violations:\n  - bla\n    blu\n  - bla\n    blu");
        assertMatch(matcher, reportReq);
        assertMatch(matcher, reportRes);
    }

    @Test
    public void checks() {
        final Matcher<RamlReport> matcher = RamlMatchers.checks();
        assertMatch(matcher, reportVal);
        assertNoMatch(matcher, reportReq, "\nRequest violations:\n  - bla");
        assertNoMatch(matcher, reportRes, "\nResponse violations:\n  - bla");
    }

    @Test
    public void request() {
        final Matcher<RamlReport> matcher = RamlMatchers.requestChecks();
        assertMatch(matcher, reportVal);
        assertNoMatch(matcher, reportReq, "\nRequest violations:\n  - bla");
        assertMatch(matcher, reportRes);
    }

    @Test
    public void response() {
        final Matcher<RamlReport> matcher = RamlMatchers.responseChecks();
        assertMatch(matcher, reportVal);
        assertMatch(matcher, reportReq);
        assertNoMatch(matcher, reportRes, "\nResponse violations:\n  - bla");
    }

    private void assertMatch(Matcher<RamlReport> matcher, RamlReport report) {
        assertTrue(matcher.matches(report));
    }

    private void assertNoMatch(Matcher<RamlReport> matcher, RamlReport report, String msg) {
        final StringDescription md = new StringDescription();
        matcher.describeMismatch(report, md);

        assertFalse(matcher.matches(report));
        assertEquals(msg, md.toString());
    }

    private RamlReport report(String validation, String request, String response) {
        final RamlReport report = new RamlReport(null);
        if (validation != null) {
            final RamlViolations req = report.getValidationViolations();
            req.add(new Message(validation));
            req.add(new Message(validation));
        }
        if (request != null) {
            report.getRequestViolations().add(new Message(request));
        }
        if (response != null) {
            report.getResponseViolations().add(new Message(response));
        }
        return report;
    }
}
