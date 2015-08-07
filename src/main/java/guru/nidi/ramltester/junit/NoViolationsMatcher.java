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
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

/**
 *
 */
class NoViolationsMatcher extends TypeSafeMatcher<RamlReport> {
    private final boolean validation, request, response;

    public NoViolationsMatcher(boolean validation, boolean request, boolean response) {
        this.validation = validation;
        this.request = request;
        this.response = response;
    }

    @Override
    protected boolean matchesSafely(RamlReport report) {
        return (!validation || report.getValidationViolations().isEmpty()) &&
                (!request || report.getRequestViolations().isEmpty()) &&
                (!response || report.getResponseViolations().isEmpty());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("To be empty");
    }

    @Override
    protected void describeMismatchSafely(RamlReport item, Description description) {
        if (validation) {
            describeList(description, "\nValidation violations:", item.getValidationViolations().asList());
        }
        if (request) {
            describeList(description, "\nRequest violations:", item.getRequestViolations().asList());
        }
        if (response) {
            describeList(description, "\nResponse violations:", item.getResponseViolations().asList());
        }
    }

    private void describeList(Description description, String start, List<String> list) {
        if (!list.isEmpty()) {
            description.appendText(start);
            for (final String s : list) {
                description.appendText("\n  - ").appendText(s.replace("\n", "\n    "));
            }
        }
    }
}
