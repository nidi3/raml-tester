package guru.nidi.ramltester.util;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

/**
 *
 */
public class TestUtils {
    private TestUtils() {
    }

    public static String getEnv(String name) {
        final String env = System.getenv(name);
        assumeThat("Environment variable " + name + " is not set, skipping test", env, notNullValue());
        return env;
    }

    public static void assertStringArrayMapEquals(Object[] expected, Map<String, String[]> actual) {
        Map<String, String[]> v = stringArrayMapOf(expected);
        assertEquals(v.size(), actual.size());
        for (Map.Entry<String, String[]> entry : v.entrySet()) {
            assertArrayEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    public static Map<String, String[]> stringArrayMapOf(Object... keysAndValues) {
        Map<String, String[]> v = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            String[] value = keysAndValues[i + 1] instanceof String
                    ? new String[]{(String) keysAndValues[i + 1]}
                    : (String[]) keysAndValues[i + 1];
            v.put((String) keysAndValues[i], value);
        }
        return v;
    }

}
