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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlTester;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 *
 */
public class RamlMatcher implements ResultMatcher {
    private final RamlTester tester;
    private final String servletUri;

    public RamlMatcher(RamlTester tester, String servletUri) {
        this.tester = tester;
        this.servletUri = servletUri;
    }

    public RamlMatcher assumingServletUri(String servletUri) {
        return new RamlMatcher(tester, servletUri);
    }

    @Override
    public void match(MvcResult result) throws Exception {
        final RamlReport report = testAgainst(result);
        if (!report.isEmpty()) {
            throw new AssertionError(report.toString());
        }
    }

    public RamlReport testAgainst(MvcResult result) {
        return tester.test(
                new SpringMockRamlRequest(servletUri, result.getRequest()),
                new SpringMockRamlResponse(result.getResponse()));
    }
}
