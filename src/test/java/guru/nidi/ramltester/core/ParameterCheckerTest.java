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

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.Message;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.raml.model.ParamType;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.QueryParameter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static guru.nidi.ramltester.util.TestUtils.stringArrayMapOf;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 *
 */
public class ParameterCheckerTest extends CoreTestBase {

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH); //to ensure . as decimal separator
    }

    @Test
    public void booleanType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.BOOLEAN);
        for (String value : new String[]{"true", "false" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "TRUE", "yes", "0", "bla" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.INTEGER);
        for (String value : new String[]{"0", "-1", "123456789" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid integer"));
        }
    }

    @Test
    public void limitedIntegerType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.INTEGER);
        p.setMinimum(BigDecimal.valueOf(-5));
        p.setMaximum(BigDecimal.valueOf(666));
        for (String value : new String[]{"-5", "0", "666" }) {
            assertNoViolation(p, value);
        }
        assertOneViolationThat(p, "-6", equalTo("BaseUri parameter 'xxx' on action - Value '-6' is smaller than minimum -5"));
        assertOneViolationThat(p, "667", equalTo("BaseUri parameter 'xxx' on action - Value '667' is bigger than maximum 666"));
    }

    @Test
    public void numberType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.NUMBER);
        for (String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "-0", "1.", "1.123w" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid number"));
        }
    }

    @Test
    public void limitedNumberType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.NUMBER);
        p.setMinimum(BigDecimal.valueOf(.05));
        p.setMaximum(BigDecimal.valueOf(666.6));
        for (String value : new String[]{"5e-2", "0.05", "666.6" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"4.9e-2", "0.0049999" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is smaller than minimum 0.05"));
        }
        for (String value : new String[]{"666.60001" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is bigger than maximum 666.6"));
        }
        for (String value : new String[]{"inf", "-inf", "nan" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not inside any minimum/maximum"));
        }
    }

    @Test
    public void dateType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.DATE);
        for (String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        for (String value : new String[]{"aa", "12345" }) {
            assertNoViolation(p, value);
        }
    }

    @Test
    public void lengthLimitedStringType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setMinLength(2);
        p.setMaxLength(5);
        assertOneViolationThat(p, "a", equalTo("BaseUri parameter 'xxx' on action - Value 'a' is shorter than minimum length 2"));
        assertOneViolationThat(p, "123456", equalTo("BaseUri parameter 'xxx' on action - Value '123456' is longer than maximum length 5"));
    }

    @Test
    public void enumLimitedStringType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setEnumeration(Arrays.asList("a", "b"));
        for (String value : new String[]{"a", "b" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "ab", "c" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a member of enum '[a, b]'"));
        }
    }

    @Test
    public void patternLimitedStringType() {
        doPatternLimitedStringType("\\d{2}/[a-y]");
        doPatternLimitedStringType("'\\d{2}/[a-y]'");
        doPatternLimitedStringType("\"\\d{2}/[a-y]\"");
        doPatternLimitedStringType("/\\d{2}\\/[a-y]/");
    }

    private void doPatternLimitedStringType(String pattern) {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setPattern(pattern);
        for (String value : new String[]{"12/a", "00/y" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "12/z", "1/a", "99/A" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' does not match pattern '" + pattern + "'"));
        }
    }

    @Test
    public void caseInsensitivePatternLimitedStringType() {
        final QueryParameter p = new QueryParameter();
        p.setType(ParamType.STRING);
        p.setPattern("/\\d{2}/[a-y]/i");
        for (String value : new String[]{"12/a", "00/y", "99/A" }) {
            assertNoViolation(p, value);
        }
        for (String value : new String[]{"", "12/z", "1/a" }) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' does not match pattern '/\\d{2}/[a-y]/i'"));
        }
    }

    @Test
    public void undefinedParameter() {
        assertOneViolationThat(queryParameterMapOf(), stringArrayMapOf("a", "b"),
                equalTo("BaseUri parameter 'a' on action is not defined"));
    }

    @Test
    public void illegallyRepeatedParameter() {
        assertOneViolationThat(queryParameterMapOf("req", new QueryParameter()), stringArrayMapOf("req", new String[]{"a", "b" }),
                equalTo("BaseUri parameter 'req' on action is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatParameter() {
        final QueryParameter p = new QueryParameter();
        p.setRepeat(true);
        assertNoViolation(queryParameterMapOf("rep", p), stringArrayMapOf("rep", new String[]{"a", "b" }));
    }

    @Test
    public void missingRequiredParameter() {
        final QueryParameter p = new QueryParameter();
        p.setRequired(true);
        assertOneViolationThat(queryParameterMapOf("req", p), stringArrayMapOf(),
                equalTo("BaseUri parameter 'req' on action is required but not found"));
    }

    private void assertNoViolation(AbstractParam param, String value) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker(violations).checkParameter(param, value, new Message("baseUriParam", "action", "xxx"));
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(AbstractParam param, String value, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker(violations).checkParameter(param, value, new Message("baseUriParam", "action", "xxx"));
        assertOneViolationThat(violations, matcher);
    }

    private void assertNoViolation(Map<String, ? extends AbstractParam> params, Values values) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker(violations).checkParameters(params, values, new Message("baseUriParam", "action"));
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(Map<String, ? extends AbstractParam> params, Values values, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker(violations).checkParameters(params, values, new Message("baseUriParam", "action"));
        assertOneViolationThat(violations, matcher);
    }
}
