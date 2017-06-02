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
package guru.nidi.ramltester.util;

import guru.nidi.ramltester.model.Values;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class FormDecoderTest {
    @Test
    public void supportsFormParams() {
        assertTrue(new FormDecoder(null, MediaType.FORM_URL_ENCODED).supportsFormParameters());
        assertTrue(new FormDecoder(null, MediaType.MULTIPART).supportsFormParameters());
        assertFalse(new FormDecoder(null, MediaType.JSON).supportsFormParameters());
    }

    @Test
    public void decodeUnsupported() {
        assertEquals(0, new FormDecoder(null, (String) null).decode().size());
        assertEquals(0, new FormDecoder(null, "").decode().size());
        assertEquals(0, new FormDecoder(null, "bla").decode().size());
        assertEquals(0, new FormDecoder(null, "text/plain").decode().size());
    }

    @Test
    public void decodeUrlEncoded() throws UnsupportedEncodingException {
        assertEquals(new Values().addValue("a", "1").addValue("b", "ä ü ö"),
                new FormDecoder("a=1&b=ä+ü%20ö".getBytes("iso-8859-1"), MediaType.FORM_URL_ENCODED).decode());
        assertEquals(new Values().addValue("a", "1").addValue("b", "ä ü ö"),
                new FormDecoder("a=1&b=ä+ü%20ö".getBytes("utf-8"), MediaType.FORM_URL_ENCODED.toString() + ";charset=utf-8").decode());
        assertNotEquals(new Values().addValue("a", "1").addValue("b", "ä ü ö"),
                new FormDecoder("a=1&b=ä+ü%20ö".getBytes("utf-8"), MediaType.FORM_URL_ENCODED).decode());
    }

    @Test
    public void decodeMultipart() throws UnsupportedEncodingException {
        assertEquals(new Values().addValue("a", "1").addValue("b", "ä+ü%20ö"),
                new FormDecoder(("--x---x\r\nContent-Disposition: form-data; name=a\r\n\r\n1\r\n" +
                        "--x---x\r\nContent-Disposition: form-data;name=b\r\n\r\nä+ü%20ö\r\n--x---x--\r\n").getBytes("iso-8859-1"),
                        MediaType.MULTIPART.toString() + ";boundary=x---x").decode());
    }
}
