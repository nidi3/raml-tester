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

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ValuesTest {
    private Values values;

    @Before
    public void init() {
        final Map<String, String[]> vals = new HashMap<>();
        vals.put("a", new String[]{"1"});
        vals.put("b", new String[]{"1", "2"});
        values = new Values(vals);
    }

    @Test
    public void size() {
        assertEquals(2, values.size());
    }

    @Test
    public void get() {
        assertEquals(Arrays.asList("1"), values.get("a"));
        assertEquals(Arrays.asList("1", "2"), values.get("b"));
        assertNull(values.get("c"));
    }

    @Test
    public void addValue() {
        values.addValue("a", "3");
        values.addValue("c", "3");
        assertEquals(Arrays.asList("1", "3"), values.get("a"));
        assertEquals(Arrays.asList("3"), values.get("c"));
    }

    @Test
    public void addValues() {
        values.addValues("a", Arrays.asList("3", "4"));
        values.addValues("c", Arrays.asList(3, 4));
        assertEquals(Arrays.asList("1", "3", "4"), values.get("a"));
        assertEquals(Arrays.asList(3, 4), values.get("c"));
    }

    @Test
    public void addValues2() {
        final Map<String, String[]> vals = new HashMap<>();
        vals.put("a", new String[]{"1"});
        vals.put("c", new String[]{"1", "2"});
        values.addValues(new Values(vals));

        assertEquals(Arrays.asList("1", "1"), values.get("a"));
        assertEquals(Arrays.asList("1", "2"), values.get("c"));
    }

    @Test
    public void setValue() {
        values.setValue("a", "3");
        values.setValue("c", "3");
        assertEquals(Arrays.asList("3"), values.get("a"));
        assertEquals(Arrays.asList("3"), values.get("c"));
    }

    @Test
    public void names() {
        assertEquals(new HashSet<>(Arrays.asList("a", "b")), values.names());
    }

    @Test
    public void asMap() {
        final Map<String, List<Object>> map = values.asMap();
        assertEquals(Arrays.asList("1"), map.get("a"));
        assertEquals(Arrays.asList("1", "2"), map.get("b"));
    }

}
