package guru.nidi.ramltester.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 *
 */
public class IoUtils {
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
                int read = reader.read(buf);
                sb.append(buf, 0, read);
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    public static byte[] readIntoByteArray(InputStream in) throws IOException {
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
