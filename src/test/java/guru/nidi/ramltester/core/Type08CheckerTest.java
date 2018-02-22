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
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.model.internal.ParameterTestImpl;
import guru.nidi.ramltester.util.Message;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.raml.v2.api.model.v08.parameters.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static guru.nidi.ramltester.junit.RamlMatchers.isEmpty;
import static guru.nidi.ramltester.util.TestUtils.valuesOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class Type08CheckerTest extends CoreTestBase {

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH); //to ensure . as decimal separator
    }

    @Test
    public void booleanType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("boolean");
        for (final String value : new String[]{"true", "false"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "TRUE", "yes", "0", "bla"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid boolean"));
        }
    }

    @Test
    public void integerType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("integer");
        for (final String value : new String[]{"0", "-1", "123456789"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "-0", "+1", "1.", "1.0", "123456x"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid integer"));
        }
    }

    @Test
    public void limitedIntegerType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("integer");
        p.setMinimum(-5);
        p.setMaximum(666);
        for (final String value : new String[]{"-5", "0", "666"}) {
            assertNoViolation(p, value);
        }
        assertOneViolationThat(p, "-6", equalTo("BaseUri parameter 'xxx' on action - Value '-6' is smaller than minimum -5"));
        assertOneViolationThat(p, "667", equalTo("BaseUri parameter 'xxx' on action - Value '667' is bigger than maximum 666"));
    }

    @Test
    public void numberType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("number");
        for (final String value : new String[]{"0", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid number"));
        }
    }

    @Test
    public void limitedNumberType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("number");
        p.setMinimum(.05);
        p.setMaximum(666.6);
        for (final String value : new String[]{"5e-2", "0.05", "666.6"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"4.9e-2", "0.0049999"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is smaller than minimum 0.05"));
        }
        for (final String value : new String[]{"666.60001"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is bigger than maximum 666.6"));
        }
    }

    @Test
    public void dateType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("date");
        for (final String value : new String[]{"Fri, 28 Feb 2014 12:34:56 GMT"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "Fri, 28 Feb 2014 12:34:56 CET", "Mon, 28 Feb 2014 12:34:56 GMT", "Sat, 29 Feb 2014 12:34:56 GMT", "Fri, 28 Feb 14 12:34:56 GMT", "Fri, 28 Feb 2014 12:34:62 GMT"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid date"));
        }
    }

    @Test
    public void stringType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("string");
        for (final String value : new String[]{"aa", "12345"}) {
            assertNoViolation(p, value);
        }
    }

    @Test
    public void lengthLimitedStringType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("string");
        p.setMinLength(2);
        p.setMaxLength(5);
        assertOneViolationThat(p, "a", equalTo("BaseUri parameter 'xxx' on action - Value 'a' is shorter than minimum length 2"));
        assertOneViolationThat(p, "123456", equalTo("BaseUri parameter 'xxx' on action - Value '123456' is longer than maximum length 5"));
    }

    @Test
    public void enumLimitedStringType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("string");
        p.setEnumeration(Arrays.asList("a", "b"));
        for (final String value : new String[]{"a", "b"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "ab", "c"}) {
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
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("string");
        p.setPattern(pattern);
        for (final String value : new String[]{"12/a", "00/y"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "12/z", "1/a", "99/A"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' does not match pattern '" + pattern + "'"));
        }
    }

    @Test
    public void caseInsensitivePatternLimitedStringType() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setType("string");
        p.setPattern("/\\d{2}/[a-y]/i");
        for (final String value : new String[]{"12/a", "00/y", "99/A"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "12/z", "1/a"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' does not match pattern '/\\d{2}/[a-y]/i'"));
        }
    }

    @Test
    public void undefinedParameter() {
        assertOneViolationThat(Collections.<Parameter>emptyList(), valuesOf("a", "b"),
                equalTo("BaseUri parameter 'a' on action is not defined"));
    }

    @Test
    public void illegallyRepeatedParameter() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setName("req");
        assertOneViolationThat(Arrays.<Parameter>asList(p), valuesOf("req", new String[]{"a", "b"}),
                equalTo("BaseUri parameter 'req' on action is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatParameter() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setRepeat(true);
        p.setName("rep");
        assertNoViolation(Arrays.<Parameter>asList(p), valuesOf("rep", new String[]{"a", "b"}));
    }

    @Test
    public void missingRequiredParameter() {
        final ParameterTestImpl p = new ParameterTestImpl();
        p.setName("req");
        p.setRequired(true);
        assertOneViolationThat(Arrays.<Parameter>asList(p), valuesOf(),
                equalTo("BaseUri parameter 'req' on action is required but not found"));
    }

    private void assertNoViolation(ParameterTestImpl param, String value) {
        final RamlViolations violations = new RamlViolations();
        new TypeChecker(violations).check(param.asType08(), value, new Message("baseUriParam", "action", "xxx"));
        assertThat(violations, isEmpty());
    }

    private void assertOneViolationThat(ParameterTestImpl param, String value, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new TypeChecker(violations).check(param.asType08(), value, new Message("baseUriParam", "action", "xxx"));
        assertOneViolationThat(violations, matcher);
    }

    private void assertNoViolation(List<Parameter> params, Values values) {
        final RamlViolations violations = new RamlViolations();
        new TypeChecker(violations).check(ParameterTestImpl.asType08(params), values, new Message("baseUriParam", "action"));
        assertThat(violations, isEmpty());
    }

    private void assertOneViolationThat(List<Parameter> params, Values values, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new TypeChecker(violations).check(ParameterTestImpl.asType08(params), values, new Message("baseUriParam", "action"));
        assertOneViolationThat(violations, matcher);
    }

}
