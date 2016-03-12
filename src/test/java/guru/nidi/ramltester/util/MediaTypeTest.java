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
package guru.nidi.ramltester.util;

import org.junit.Test;

import java.util.Collections;

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
        assertEquals(Collections.singletonMap("a", "b"), mediaType.getParameters());
    }

    @Test
    public void wildcardTypeCompatibility() {
        assertTrue(compatible("a/b", "*"));
        assertTrue(compatible("*", "a/b"));
    }

    @Test
    public void wildcardSubtypeCompatibility() {
        assertTrue(compatible("a/b", "a/*"));
        assertFalse(compatible("a/b", "a/*+bla"));
        assertTrue(compatible("a/b+bla", "a/*+bla"));
    }

    @Test
    public void typeCompatibility() {
        assertFalse(compatible("a/b", "c/b"));
        assertFalse(compatible("a/b", "a/c"));
        assertTrue(compatible("a/b", "a/b"));
        assertTrue(compatible("a/b", "a/b;x=y"));
    }

    @Test
    public void jsonCompatibility() {
        assertTrue(compatible(MediaType.JSON.toString(), "a/b+json"));
        assertTrue(compatible("a/b+json", MediaType.JSON.toString()));
    }

    @Test
    public void suffixWildcardSimilarity() {
        assertTrue(anySimilar("a/b+c", "a/*+c"));
        assertTrue(similarer("a/b+c", "a/b+c", "a/*+c"));
        assertFalse(anySimilar("a/b+c", "a/*+d"));
    }

    @Test
    public void jsonSimilarity() {
        similarity("application/json");
        similarity("a/b+json");
    }

    @Test
    public void similarityExamples() {
        assertTrue(similarer("application/ld+json;charset=ISO-8859-1", "application/ld+json", "application/vnd.geo+json"));
    }

    @Test
    public void onlyTypeMatchingIsNotSimilar() {
        assertFalse(anySimilar("a/b", "a/c"));
        assertTrue(anySimilar("a/b", "*/*"));
    }

    @Test
    public void longerMatchIsMoreSimilar() {
        assertTrue(similarer("a/b;x=y", "a/b;x=y", "a/b"));
        assertTrue(similarer("a/b+json", "a/x+json", "application/json"));
        assertTrue(similarer("a/b+json", "a/b+json", "a/x+json"));
        assertTrue(similarer("a/b+json;x=y", "a/b+json;x=y", "a/b+json"));
    }

    @Test
    public void wildcardMatchIsLessSimilarThanExact() {
        assertTrue(similarer("a/b", "a/*", "*/*"));
        assertTrue(similarer("a/b", "a/b", "a/*"));
        assertTrue(similarer("a/b+json", "a/b+json", "a/*+json"));
    }

    @Test
    public void wildcardMatchIsMoreSimilarThanNone() {
        assertTrue(similarer("a/b+json", "a/*+json", "a/c+json"));
    }

    @Test
    public void qualityParamOk() {
        assertEquals(.5, valueOf("a/b;q=.5").getQualityParameter(), .0000001);
        assertEquals(0, valueOf("a/b;q='0'").getQualityParameter(), .0000001);
        assertEquals(1, valueOf("a/b;q=\"1\"").getQualityParameter(), .0000001);
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void qualityParamNotNumeric() {
        valueOf("a/b;q=a");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void qualityParamTooLow() {
        valueOf("a/b;q=-.001");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void qualityParamTooHigh() {
        valueOf("a/b;q=1.001");
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void qualityParamEmpty() {
        valueOf("a/b;q=");
    }

    @Test
    public void charset() {
        assertEquals("cs", valueOf("a/b; charset=cs").getCharset("def"));
        assertEquals("def", valueOf("a/b").getCharset("def"));
    }

    private boolean compatible(String base, String type) {
        return valueOf(base).isCompatibleWith(valueOf(type));
    }

    private void similarity(String complete) {
        assertTrue(similarer("application/json;c=d", "application/json;c=d", complete));
        assertTrue(similarer(complete, complete, "application/*"));
        assertTrue(similarer(complete, "application/*", "*/*"));
        assertFalse(anySimilar(complete, "application/c"));

        assertTrue(asSimilar("application/*", complete, "application/c"));
    }

    private boolean anySimilar(String base, String type) {
        return valueOf(base).similarity(valueOf(type)) > 0;
    }

    private boolean similarer(String base, String type1, String type2) {
        return valueOf(base).similarity(valueOf(type1)) > valueOf(base).similarity(valueOf(type2));
    }

    private boolean asSimilar(String base, String type1, String type2) {
        return valueOf(base).similarity(valueOf(type1)) == valueOf(base).similarity(valueOf(type2));
    }

}