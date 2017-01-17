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

import org.junit.Test;

import static guru.nidi.ramltester.util.TestUtils.assertValuesEquals;
import static org.junit.Assert.assertEquals;

public class VariableMatcherTest extends CoreTestBase {
    @Test
    public void noVariables() {
        assertMatch("abc", "abc", true, true, "");
        assertMatch("abc", "abcde", true, false, "de");
        assertMatch("abc", "ade", false, false, "");
    }

    @Test
    public void oneVariable() {
        assertMatch("abc{var}xyz", "abc123xyz", true, true, "", "var", "123");
        assertMatch("abc{var}xyz", "abc123xy", false, false, "", "var", "123");
        assertMatch("abc{var}xyz", "abc123xyz000", true, false, "000", "var", "123");
        assertMatch("abc{var}", "abc123xyz", true, true, "", "var", "123xyz");
        assertMatch("abc{var}", "abc123/xyz", true, false, "/xyz", "var", "123");
        assertMatch("abc{var}", "abc123&xyz", true, false, "&xyz", "var", "123");
        assertMatch("abc{var}&", "abc123&xyz", true, false, "xyz", "var", "123");
        assertMatch("abc{var}", "abc123-._~;", true, false, ";", "var", "123-._~");
        assertMatch("abc{var}x", "abc12/x", false, false, "");
    }

    @Test
    public void reserved(){
        assertMatch("abc{+var}x", "abc123&xyz", true, false, "yz", "var", "123&");
        assertMatch("abc{+var}x", "abc123/xyz", true, false, "yz", "var", "123/");
        assertMatch("abc{+var}x", "abc123%xyz", false, false, "");
    }

    @Test
    public void hashed() {
        assertMatch("abc{#var}xyz", "abc#123xyz", true, true, "", "var", "123");
        assertMatch("abc{#var}xyz", "abc#123&xyz", true, true, "", "var", "123&");
        assertMatch("abc{#var}", "abc#123xyz", true, true, "", "var", "123xyz");
        assertMatch("abc{#var}", "abc#123%xyz", true, false, "%xyz", "var", "123");
        assertMatch("abc{#var}", "abc123xyz", false, false, "");
    }

    @Test
    public void multiVariables() {
        assertMatch("abc{var}/{two}", "abc123/xyz", true, true, "", "var", "123", "two", "xyz");
        assertMatch("abc{var}/{two}", "abc123/xyz/abc", true, false, "/abc", "var", "123", "two", "xyz");
    }

    @Test
    public void multiValues() {
        assertMatch("abc{var}/{var}", "abc123/xyz", true, true, "", "var", new String[]{"123", "xyz"});
    }

    @Test(expected = IllegalVariablePatternException.class)
    public void invalidPattern() {
        new VariableMatcher("abc{var", "abc123xyz").match();
    }

    private void assertMatch(String pattern, String value, boolean matches, boolean completeMatch, String suffix, Object... variables) {
        doAssertMatch(new VariableMatcher(pattern, value).match(), matches, completeMatch, suffix, variables);
    }

    private void doAssertMatch(VariableMatcher.Match vm, boolean matches, boolean completeMatch, String suffix, Object... variables) {
        assertEquals(matches, vm.matches);
        assertEquals(completeMatch, vm.completeMatch);
        assertEquals(suffix, vm.suffix);
        assertValuesEquals(variables, vm.variables);
    }
}
