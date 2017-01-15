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
package guru.nidi.ramltester.core;

import java.util.*;

class CountSet<T> extends AbstractSet<T> {
    private final Map<T, Integer> map = new HashMap<>();

    public void add(T value, int count) {
        final int old = getCount(value);
        map.put(value, old + count);
    }

    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    public boolean add(T value) {
        add(value, 1);
        return true;
    }

    public void addAll(Collection<? extends T> values, int count) {
        for (final T value : values) {
            add(value, count);
        }
    }

    public boolean addAll(Collection<? extends T> values) {
        addAll(values, 1);
        return true;
    }

    public int getCount(T value) {
        final Integer count = map.get(value);
        return count == null ? 0 : count;
    }

    public Iterable<Map.Entry<T, Integer>> values() {
        return new Iterable<Map.Entry<T, Integer>>() {
            @Override
            public Iterator<Map.Entry<T, Integer>> iterator() {
                return map.entrySet().iterator();
            }
        };
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
