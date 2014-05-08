package guru.nidi.ramltester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class VariableMatcherTest extends TestBase {
    @Test
    public void noVariables() {
        assertMatch(VariableMatcher.match("abc", "abc"), true, true, "");
        assertMatch(VariableMatcher.match("abc", "abcde"), true, false, "de");
        assertMatch(VariableMatcher.match("abc", "ade"), false, false, "");
    }

    @Test
    public void oneVariable() {
        assertMatch(VariableMatcher.match("abc{var}xyz", "abc123xyz"), true, true, "", "var", "123");
        assertMatch(VariableMatcher.match("abc{var}xyz", "abc123xy"), false, false, "", "var", "123");
        assertMatch(VariableMatcher.match("abc{var}xyz", "abc123xyz000"), true, false, "000", "var", "123");
        assertMatch(VariableMatcher.match("abc{var}", "abc123xyz"), true, true, "", "var", "123xyz");
        assertMatch(VariableMatcher.match("abc{var}", "abc123/xyz"), true, false, "/xyz", "var", "123");
    }

    @Test
    public void multiVariables() {
        assertMatch(VariableMatcher.match("abc{var}/{two}", "abc123/xyz"), true, true, "", "var", "123", "two", "xyz");
        assertMatch(VariableMatcher.match("abc{var}/{two}", "abc123/xyz/abc"), true, false, "/abc", "var", "123", "two", "xyz");
    }

    @Test
    public void multiValues() {
        assertMatch(VariableMatcher.match("abc{var}/{var}", "abc123/xyz"), true, true, "", "var", new String[]{"123", "xyz"});
    }

    @Test(expected = IllegalVariablePatternException.class)
    public void invalidPattern() {
        VariableMatcher.match("abc{var", "abc123xyz");
    }

    private void assertMatch(VariableMatcher vm, boolean matches, boolean completeMatch, String suffix, Object... variables) {
        assertEquals(matches, vm.isMatch());
        assertEquals(completeMatch, vm.isCompleteMatch());
        assertEquals(suffix, vm.getSuffix());
        assertStringArrayMapEquals(variables, vm.getVariables().getValues());
    }


}
