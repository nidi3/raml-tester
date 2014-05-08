package guru.nidi.ramltester;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.raml.model.ParamType;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.QueryParameter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public class ParameterTesterTest extends TestBase {
    @Test
    public void booleanType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.BOOLEAN);
        for (String value : new String[]{"true", "false"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.INTEGER);
        for (String value : new String[]{"0", "-1", "123456789"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is not a valid integer"));
        }
    }

    @Test
    public void limitedIntegerType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.INTEGER);
        p.setMinimum(BigDecimal.valueOf(-5));
        p.setMaximum(BigDecimal.valueOf(666));
        for (String value : new String[]{"-5", "0", "666"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"-6", "667"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is "));
        }
    }

    @Test
    public void numberType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.NUMBER);
        for (String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is not a valid number"));
        }
    }

    @Test
    public void limitedNumberType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.NUMBER);
        p.setMinimum(BigDecimal.valueOf(.05));
        p.setMaximum(BigDecimal.valueOf(666.6));
        for (String value : new String[]{"5e-2", "0.05", "666.6"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"4.9e-2", "0.0049999", "666.60001", "inf", "-inf", "nan"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is "));
        }
    }

    @Test
    public void dateType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.DATE);
        for (String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        for (String value : new String[]{"aa", "12345"}) {
            assertNoViolation(p, value);
        }
    }

    @Test
    public void lengthLimitedStringType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setMinLength(2);
        p.setMaxLength(5);
        for (String value : new String[]{"", "a", "123456"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is "));
        }
    }

    @Test
    public void enumLimitedStringType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setEnumeration(Arrays.asList("a", "b"));
        for (String value : new String[]{"a", "b"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "ab", "c"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' is not a member of enum '[a, b]'"));
        }
    }

    @Test
    public void patternLimitedStringType() throws Exception {
        doPatternLimitedStringType("\\d{2}/[a-y]");
        doPatternLimitedStringType("'\\d{2}/[a-y]'");
        doPatternLimitedStringType("\"\\d{2}/[a-y]\"");
        doPatternLimitedStringType("/\\d{2}\\/[a-y]/");
    }

    private void doPatternLimitedStringType(String pattern) {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setPattern(pattern);
        for (String value : new String[]{"12/a", "00/y"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' does not match pattern '" + pattern + "'"));
        }
    }

    @Test
    public void caseInsensitivePatternLimitedStringType() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setPattern("/\\d{2}/[a-y]/i");
        for (String value : new String[]{"12/a", "00/y", "99/A"}) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "12/z", "1/a"}) {
            assertOneViolationThat(p, value,
                    startsWith("desc: Value '" + value + "' does not match pattern '/\\d{2}/[a-y]/i'"));
        }
    }

    @Test
    public void undefinedParameter() throws Exception {
        assertOneViolationThat(this.<AbstractParam>mapOf(), stringArrayMapOf("a", "b"),
                startsWith("desc 'a' is not defined"));
    }

    @Test
    public void illegallyRepeatedParameter() throws Exception {
        assertOneViolationThat(this.<QueryParameter>mapOf("req", new QueryParameter()), stringArrayMapOf("req", new String[]{"a", "b"}),
                startsWith("desc 'req' is not repeat but found repeatedly in response"));
    }

    @Test
    public void allowedRepeatParameter() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setRepeat(true);
        assertNoViolation(this.<QueryParameter>mapOf("rep", p), stringArrayMapOf("rep", new String[]{"a", "b"}));
    }

    @Test
    public void missingRequiredParameter() throws Exception {
        final QueryParameter p = new QueryParameter();
        p.setRequired(true);
        assertOneViolationThat(this.<QueryParameter>mapOf("req", p), stringArrayMapOf(),
                startsWith("desc 'req' is required but not found in response"));
    }

    private void assertNoViolation(AbstractParam param, String value) {
        final RamlViolations violations = new RamlViolations();
        new ParameterTester(violations, false).testParameter(param, value, "desc");
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(AbstractParam param, String value, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterTester(violations, false).testParameter(param, value, "desc");
        assertOneViolationThat(violations, matcher);
    }

    private void assertNoViolation(Map<String, ? extends AbstractParam> params, Map<String, String[]> values) {
        final RamlViolations violations = new RamlViolations();
        new ParameterTester(violations, false).testParameters(params, values, "desc");
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(Map<String, ? extends AbstractParam> params, Map<String, String[]> values, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterTester(violations, false).testParameters(params, values, "desc");
        assertOneViolationThat(violations, matcher);
    }
}
