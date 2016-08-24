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
import org.raml.v2.api.model.v08.parameters.NumberTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.parameters.StringTypeDeclaration;
import org.raml.v2.api.model.v08.system.types.MarkdownString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static guru.nidi.ramltester.util.TestUtils.valuesOf;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 *
 */
public class ParameterChecker08Test extends CoreTestBase {

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH); //to ensure . as decimal separator
    }

    @Test
    public void booleanType() {
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
        p.setType("number");
        for (final String value : new String[]{"0", "inf", "-inf", "nan", "-1", "-.1", "1e-1", "1e+1", "1e1", "1.2345e-1123"}) {
            assertNoViolation(p, value);
        }
        for (final String value : new String[]{"", "-0", "1.", "1.123w"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not a valid number"));
        }
    }

    @Test
    public void limitedNumberType() {
        final ParameterImpl p = new ParameterImpl();
        p.setType("integer");
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
        for (final String value : new String[]{"inf", "-inf", "nan"}) {
            assertOneViolationThat(p, value,
                    equalTo("BaseUri parameter 'xxx' on action - Value '" + value + "' is not inside any minimum/maximum"));
        }
    }

    @Test
    public void dateType() {
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
        p.setType("string");
        for (final String value : new String[]{"aa", "12345"}) {
            assertNoViolation(p, value);
        }
    }

    @Test
    public void lengthLimitedStringType() {
        final ParameterImpl p = new ParameterImpl();
        p.setType("string");
        p.setMinLength(2);
        p.setMaxLength(5);
        assertOneViolationThat(p, "a", equalTo("BaseUri parameter 'xxx' on action - Value 'a' is shorter than minimum length 2"));
        assertOneViolationThat(p, "123456", equalTo("BaseUri parameter 'xxx' on action - Value '123456' is longer than maximum length 5"));
    }

    @Test
    public void enumLimitedStringType() {
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
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
        final ParameterImpl p = new ParameterImpl();
        p.setName("req");
        assertOneViolationThat(Arrays.<Parameter>asList(p), valuesOf("req", new String[]{"a", "b"}),
                equalTo("BaseUri parameter 'req' on action is not repeat but found repeatedly"));
    }

    @Test
    public void allowedRepeatParameter() {
        final ParameterImpl p = new ParameterImpl();
        p.setRepeat(true);
        p.setName("rep");
        assertNoViolation(Arrays.<Parameter>asList(p), valuesOf("rep", new String[]{"a", "b"}));
    }

    @Test
    public void missingRequiredParameter() {
        final ParameterImpl p = new ParameterImpl();
        p.setName("req");
        p.setRequired(true);
        assertOneViolationThat(Arrays.<Parameter>asList(p), valuesOf(),
                equalTo("BaseUri parameter 'req' on action is required but not found"));
    }

    private void assertNoViolation(Parameter param, String value) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker08(violations).checkParameter(param, value, new Message("baseUriParam", "action", "xxx"));
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(Parameter param, String value, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker08(violations).checkParameter(param, value, new Message("baseUriParam", "action", "xxx"));
        assertOneViolationThat(violations, matcher);
    }

    private void assertNoViolation(List<Parameter> params, Values values) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker08(violations).checkParameters(params, values, new Message("baseUriParam", "action"));
        assertNoViolations(violations);
    }

    private void assertOneViolationThat(List<Parameter> params, Values values, Matcher<String> matcher) {
        final RamlViolations violations = new RamlViolations();
        new ParameterChecker08(violations).checkParameters(params, values, new Message("baseUriParam", "action"));
        assertOneViolationThat(violations, matcher);
    }

    private static class ParameterImpl implements Parameter, NumberTypeDeclaration, StringTypeDeclaration {
        private String type;
        private double minimum, maximum;
        private int minLength, maxLength;
        private List<String> enumeration;
        private String pattern;
        private boolean required, repeat;
        private String name;

        public void setType(String type) {
            this.type = type;
        }

        public void setMinimum(double minimum) {
            this.minimum = minimum;
        }

        public void setMaximum(double maximum) {
            this.maximum = maximum;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public void setEnumeration(List<String> enumeration) {
            this.enumeration = enumeration;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public void setRepeat(boolean repeat) {
            this.repeat = repeat;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String displayName() {
            return null;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public Boolean required() {
            return required;
        }

        @Override
        public String defaultValue() {
            return null;
        }

        @Override
        public String example() {
            return null;
        }

        @Override
        public Boolean repeat() {
            return repeat;
        }

        @Override
        public MarkdownString description() {
            return null;
        }

        @Override
        public Double minimum() {
            return minimum;
        }

        @Override
        public Double maximum() {
            return maximum;
        }

        @Override
        public String pattern() {
            return pattern;
        }

        @Override
        public List<String> enumValues() {
            return enumeration;
        }

        @Override
        public Integer minLength() {
            return minLength;
        }

        @Override
        public Integer maxLength() {
            return maxLength;
        }
    }
}
