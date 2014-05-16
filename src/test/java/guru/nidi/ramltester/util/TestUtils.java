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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

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

    public static Matcher<Number> biggerThan(final Number value) {
        return new TypeSafeMatcher<Number>() {
            @Override
            protected boolean matchesSafely(Number item) {
                return item.doubleValue() > value.doubleValue();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A number bigger than ").appendValue(value);
            }
        };
    }

}
