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
        assertThat("Expected exactly one violation", 1, new IsEqual<>(violations.size()));
        assertThat(violations.iterator().next(), matcher);
    }

    protected Map<String, QueryParameter> queryParameterMapOf(Object... keysAndValues) {
        return mapOf(keysAndValues);
    }

    protected <T> Map<String, T> mapOf(Object... keysAndValues) {
        Map<String, T> v = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            v.put((String) keysAndValues[i], (T) keysAndValues[i + 1]);
        }
        return v;
    }
}
