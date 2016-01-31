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
package guru.nidi.ramltester.core;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.raml.model.parameter.QueryParameter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CoreTestBase {


    protected void assertNoViolations(RamlReport report) {
        assertTrue("Expected no violations, but found: " + report, report.isEmpty());
    }

    protected void assertNoViolations(RamlViolations violations) {
        assertTrue("Expected no violations, but found: " + violations, violations.isEmpty());
    }

    protected void assertOneViolationThat(RamlViolations violations, Matcher<String> matcher) {
        assertThat("Expected exactly one violation", 1, new IsEqual<Integer>(violations.size()));
        assertThat(violations.iterator().next(), matcher);
    }

    protected Map<String, QueryParameter> queryParameterMapOf(Object... keysAndValues) {
        return mapOf(keysAndValues);
    }

    @SuppressWarnings("unchecked")
    protected <T> Map<String, T> mapOf(Object... keysAndValues) {
        final Map<String, T> v = new HashMap<String, T>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            v.put((String) keysAndValues[i], (T) keysAndValues[i + 1]);
        }
        return v;
    }
}
