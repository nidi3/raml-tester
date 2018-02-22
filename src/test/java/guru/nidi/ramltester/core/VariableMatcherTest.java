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
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class VariableMatcherTest extends CoreTestBase {
    @Test
    public void noVariables() {
        assertMatch("abc", "abc", match(""));
        assertMatch("abc", "abcde", match("de"));
        assertMatch("abc", "ade");
    }

    @Test
    public void oneVariable() {
        assertMatch("abc{var}xyz", "abc123xyz", match("", "var", "123"));
        assertMatch("abc{var}xyz", "abc123xy");
        assertMatch("abc{var}xyz", "abc123xyz000", match("000", "var", "123"));
        assertMatch("abc{var}", "abc12/xyz", match("/xyz", "var", "12"), match("2/xyz", "var", "1"), match("12/xyz", "var", ""));
        assertMatch("abc{var}", "abc12&xyz", match("&xyz", "var", "12"), match("2&xyz", "var", "1"), match("12&xyz", "var", ""));
        assertMatch("abc{var}&", "abc123&xyz", match("xyz", "var", "123"));
        assertMatch("abc{var}", "abc-._~;", match(";", "var", "-._~"), match("~;", "var", "-._"), match("_~;", "var", "-."), match("._~;", "var", "-"), match("-._~;", "var", ""));
        assertMatch("abc{var}x", "abc12/x");
    }

    @Test
    public void ambigousOneVar() {
        assertMatch("abc{var}xyz", "abc123xyzxyz", match("", "var", "123xyz"), match("xyz", "var", "123"));
    }

    @Test
    public void ambigousTwoVar() {
        assertMatch("abc{var}xy{v2}xy", "abcxyxyxy", match("xy", "var", "", "v2", ""), match("", "var", "", "v2", "xy"), match("", "var", "xy", "v2", ""));
    }

    @Test
    public void ambigousAtEnd() {
        assertMatch("abc{var}", "abc123", match("", "var", "123"), match("3", "var", "12"), match("23", "var", "1"), match("123", "var", ""));
    }

    @Test
    public void varWithoutSeparator() {
        assertMatch("abc{v1}{v2}xyz", "abc12xyz", match("", "v1", "", "v2", "12"), match("", "v1", "1", "v2", "2"), match("", "v1", "12", "v2", ""));
    }

    @Test
    public void reserved() {
        assertMatch("abc{+var}x", "abc123&xyz", match("yz", "var", "123&"));
        assertMatch("abc{+var}x", "abc123/xyz", match("yz", "var", "123/"));
        assertMatch("abc{+var}x", "abc123%xyz");
    }

    @Test
    public void hashed() {
        assertMatch("abc{#var}xyz", "abc#123xyz", match("", "var", "123"));
        assertMatch("abc{#var}xyz", "abc#123&xyz", match("", "var", "123&"));
        assertMatch("abc{#var}c", "abc#123xyzc", match("", "var", "123xyz"));
        assertMatch("abc{#var}", "abc#1%xyz", match("%xyz", "var", "1"), match("1%xyz", "var", ""));
        assertMatch("abc{#var}", "abc123xyz");
    }

    @Test
    public void multiVariables() {
        assertMatch("abc{var}/{two}/", "abc123/xyz/", match("", "var", "123", "two", "xyz"));
        assertMatch("abc{var}/{two}/", "abc123/xyz/abc", match("abc", "var", "123", "two", "xyz"));
    }

    @Test
    public void multiValues() {
        assertMatch("abc{var}/{var}/", "abc123/xyz/", match("", "var", new String[]{"123", "xyz"}));
    }

    @Test(expected = IllegalVariablePatternException.class)
    public void invalidPattern() {
        new VariableMatcher("abc{var", "abc123xyz").match();
    }

    private void assertMatch(String pattern, String value, VariableMatcher.Match... matches) {
        assertThat(new VariableMatcher(pattern, value).match(), containsInAnyOrder(matches));
    }

    private VariableMatcher.Match match(String suffix, Object... values) {
        final Values vals = new Values();
        for (int i = 0; i < values.length; i += 2) {
            if (values[i + 1].getClass().isArray()) {
                vals.addValues((String) values[i], Arrays.asList((Object[]) values[i + 1]));
            } else {
                vals.addValue((String) values[i], values[i + 1]);
            }
        }
        return new VariableMatcher.Match(suffix, vals);
    }
}
