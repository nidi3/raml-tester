package guru.nidi.ramltester.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class JsRegexTest {
    @Test
    public void simple() {
        assertTrue(JsRegex.matches("a1", "^[a-z]", null));
        assertFalse(JsRegex.matches("1a", "^[a-z]", null));
        assertFalse(JsRegex.matches("A1", "^[a-z]", null));
        assertTrue(JsRegex.matches("A1", "^[a-z]", "i"));
        assertTrue(JsRegex.matches(" ", "^\\s", null));
    }

    @Test
    public void slashed() {
        assertTrue(JsRegex.matches("a/b", "/a\\/b/"));
        assertFalse(JsRegex.matches("A/b", "/a\\/b/"));
        assertTrue(JsRegex.matches("A/b", "/a\\/b/i"));
    }

    @Test(expected = JsRegex.InvalidRegexException.class)
    public void invalidPattern() {
        JsRegex.matches("", "^[a-z", null);
    }

    @Test(expected = JsRegex.InvalidRegexException.class)
    public void invalidModifier() {
        JsRegex.matches("", "^[a-z]", "yxz");
    }
}
