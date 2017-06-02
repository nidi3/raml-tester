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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class IoUtils {
    private IoUtils() {
    }

    public static String readIntoString(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        try {
            final StringBuilder sb = new StringBuilder();
            final char[] buf = new char[1000];
            while (reader.ready()) {
                final int read = reader.read(buf);
                sb.append(buf, 0, read);
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    public static byte[] readIntoByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buf = new byte[1000];
            int read;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }
}
