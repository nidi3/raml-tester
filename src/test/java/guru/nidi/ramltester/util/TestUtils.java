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

import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.model.Values;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public class TestUtils {
    private TestUtils() {
    }

    public static String getEnv(String name) {
        final String env = System.getenv(name);
        assumeThat("Environment variable " + name + " is not set, skipping test", env, notNullValue());
        return env;
    }

    public static void assumeEnv(String name, String value) {
        assumeThat("Environment variable " + name + " has not value " + value + ", skipping test", getEnv(name), equalTo(value));
    }

    public static void assertValuesEquals(Object[] expected, Values actual) {
        final Values v = valuesOf(expected);
        assertEquals(v, actual);
    }

    public static Values valuesOf(Object... keysAndValues) {
        final Values v = new Values();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            final List<String> value = keysAndValues[i + 1] instanceof String
                    ? Arrays.asList((String) keysAndValues[i + 1])
                    : Arrays.asList((String[]) keysAndValues[i + 1]);
            v.addValues((String) keysAndValues[i], value);
        }
        return v;
    }

    public static RamlViolations violations(String... messages) {
        final RamlViolations violations = new RamlViolations();
        for (final String message : messages) {
            violations.add(new SimpleMessage(message));
        }
        return violations;
    }

    public static Map<String, Object> map(Object... keysAndValues) {
        final Map<String, Object> res = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            res.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return res;
    }

    private static class SimpleMessage extends Message {
        public SimpleMessage(String key, Object... params) {
            super(key, params);
        }

        @Override
        public String toString() {
            return key;
        }
    }

}
