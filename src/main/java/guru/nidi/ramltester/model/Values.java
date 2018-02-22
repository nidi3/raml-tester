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
package guru.nidi.ramltester.model;

import java.util.*;

public final class Values implements Iterable<Map.Entry<String, List<Object>>> {
    private final Map<String, List<Object>> values = new HashMap<>();

    public Values() {
    }

    public Values(Map<String, String[]> values) {
        for (final Map.Entry<String, String[]> entry : values.entrySet()) {
            addValues(entry.getKey(), Arrays.asList(entry.getValue()));
        }
    }

    public Values(Values values) {
        for (final Map.Entry<String, List<Object>> entry : values) {
            addValues(entry.getKey(), entry.getValue());
        }
    }

    public int size() {
        return values.size();
    }

    public List<Object> get(String name) {
        return values.get(name);
    }

    public Values addValue(String name, Object value) {
        List<Object> vs = values.get(name);
        if (vs == null) {
            vs = new ArrayList<>();
            values.put(name, vs);
        }
        vs.add(value);
        return this;
    }

    public void setValue(String name, String value) {
        final List<Object> vs = new ArrayList<>();
        vs.add(value);
        values.put(name, vs);
    }

    public void addValues(String name, Iterable<?> values) {
        for (final Object value : values) {
            addValue(name, value);
        }
    }

    public void addValues(Values values) {
        for (final Map.Entry<String, List<Object>> value : values) {
            addValues(value.getKey(), value.getValue());
        }
    }

    @Override
    public Iterator<Map.Entry<String, List<Object>>> iterator() {
        return values.entrySet().iterator();
    }

    public Set<String> names() {
        return values.keySet();
    }

    public Map<String, List<Object>> asMap() {
        return values;
    }

    public String asJson() {
        final StringBuilder s = new StringBuilder("{");
        boolean first = true;
        for (final Map.Entry<String, List<Object>> entry : this) {
            if (first) {
                first = false;
            } else {
                s.append(',');
            }
            addLine(s, entry.getKey(), entry.getValue(), true, ": ");
        }
        return s.append('}').toString();
    }

    public String asYaml() {
        return toString(": ");
    }

    public String toSimpleString() {
        return toString(" = ");
    }

    private String toString(String sep) {
        final StringBuilder s = new StringBuilder("\n");
        for (final Map.Entry<String, List<Object>> entry : this) {
            addLine(s, entry.getKey(), entry.getValue(), false, sep);
        }
        return s.toString();
    }

    private void addLine(StringBuilder s, String key, List<Object> value, boolean escKey, String sep) {
        if (escKey) {
            s.append('"').append(key).append('"');
        } else {
            s.append(key);
        }
        s.append(sep);
        final boolean isList = value.size() > 1;
        if (isList) {
            s.append('[');
        }
        s.append('"').append(value.get(0).toString()).append('"');
        if (isList) {
            s.append(']');
        }
        s.append('\n');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Values values1 = (Values) o;
        return values.equals(values1.values);

    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "Values{" + values + '}';
    }
}
