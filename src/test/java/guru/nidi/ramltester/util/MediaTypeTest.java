package guru.nidi.ramltester.util;

import org.junit.Test;

import java.util.HashMap;

import static guru.nidi.ramltester.util.MediaType.valueOf;
import static org.junit.Assert.*;

public class MediaTypeTest {
    @Test(expected = InvalidMediaTypeException.class)
    public void valueOfWithNull() {
        valueOf(null);
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void valueOfWithEmpty() {
        valueOf("");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void valueOfWithoutSlash() {
        valueOf("abc");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void valueOfWithEmptySubtype() {
        valueOf("abc/");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void valueOfWithInvlidWildcard() {
        valueOf("*/abc");
    }

    @Test
    public void allowSimpleWildcard() {
        assertEquals(valueOf("*/*"), valueOf("*"));
    }

    @Test
    public void testGetters() {
        final MediaType mediaType = valueOf("type/sub;a=b");
        assertEquals("type", mediaType.getType());
        assertEquals("sub", mediaType.getSubtype());
        assertEquals(new HashMap<String, String>() {{
            put("a", "b");
        }}, mediaType.getParameters());
    }

    @Test
    public void wildcardTypeCompatibility() {
        assertTrue(valueOf("a/b").isCompatibleWith(valueOf("*")));
        assertTrue(valueOf("*").isCompatibleWith(valueOf("a/b")));
    }

    @Test
    public void wildcardSubtypeCompatibility() {
        assertTrue(valueOf("a/b").isCompatibleWith(valueOf("a/*")));
        assertFalse(valueOf("a/b").isCompatibleWith(valueOf("a/*+bla")));
        assertTrue(valueOf("a/b+bla").isCompatibleWith(valueOf("a/*+bla")));
    }

    @Test
    public void typeCompatibility() {
        assertFalse(valueOf("a/b").isCompatibleWith(valueOf("c/b")));
        assertFalse(valueOf("a/b").isCompatibleWith(valueOf("a/c")));
        assertTrue(valueOf("a/b").isCompatibleWith(valueOf("a/b")));
        assertTrue(valueOf("a/b").isCompatibleWith(valueOf("a/b;x=y")));
    }

    @Test
    public void jsonCompatibility(){
        assertTrue(MediaType.JSON.isCompatibleWith(valueOf("a/b+json")));
        assertTrue(valueOf("a/b+json").isCompatibleWith(MediaType.JSON));
    }
}