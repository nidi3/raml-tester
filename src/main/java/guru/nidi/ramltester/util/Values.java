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

import java.util.*;

/**
 *
 */
public class Values implements Iterable<Map.Entry<String, List<String>>> {
    private final Map<String, List<String>> values = new HashMap<>();

    public Values() {
    }

    public Values(Map<String, String[]> values) {
        for (Map.Entry<String, String[]> entry : values.entrySet()) {
            addValues(entry.getKey(), Arrays.asList(entry.getValue()));
        }
    }

    public int size() {
        return values.size();
    }

    public List<String> get(String name) {
        return values.get(name);
    }

    public Values addValue(String name, String value) {
        List<String> vs = values.get(name);
        if (vs == null) {
            vs = new ArrayList<>();
            values.put(name, vs);
        }
        vs.add(value);
        return this;
    }

    public void setValue(String name, String value) {
        final List<String> vs = new ArrayList<>();
        vs.add(value);
        values.put(name, vs);
    }

    public void addValues(String name, Iterable<String> values) {
        for (String value : values) {
            addValue(name, value);
        }
    }

    public void addValues(Values values) {
        for (Map.Entry<String, List<String>> value : values) {
            addValues(value.getKey(), value.getValue());
        }
    }

    @Override
    public Iterator<Map.Entry<String, List<String>>> iterator() {
        return values.entrySet().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Values values1 = (Values) o;

        if (!values.equals(values1.values)) {
            return false;
        }

        return true;
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
