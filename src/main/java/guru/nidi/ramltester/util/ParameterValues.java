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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ParameterValues {
    private final Map<String, String[]> values = new HashMap<>();

    public Map<String, String[]> getValues() {
        return values;
    }

    public int size() {
        return values.size();
    }

    public void addValue(String name, String value) {
        final String[] newValues = appendValue(values.get(name), value);
        values.put(name, newValues);
    }

    public void addValues(String name, String[] values) {
        for (String value : values) {
            addValue(name, value);
        }
    }

    public void addValues(ParameterValues values) {
        for (Map.Entry<String, String[]> value : values.getValues().entrySet()) {
            addValues(value.getKey(), value.getValue());
        }
    }

    private String[] appendValue(String[] values, String value) {
        final String[] newValues;
        if (values == null) {
            newValues = new String[]{value};
        } else {
            newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
        }
        return newValues;
    }
}
