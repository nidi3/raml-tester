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

import guru.nidi.ramltester.util.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class RamlViolations implements Iterable<RamlViolationMessage> {
    private final List<RamlViolationMessage> messages;

    public RamlViolations() {
        messages = new ArrayList<>();
    }

    public void add(Message message) {
        add(message, null);
    }

    public void add(Message message, Object cause) {
        messages.add(new RamlViolationMessage(message.toString(),cause));
    }

    void add(String key, Object... params) {
        add(new Message(key, params));
    }

    public void addIf(boolean condition, Message message) {
        if (condition) {
            add(message);
        }
    }

    void addIf(boolean condition, String key, Object... params) {
        addIf(condition, new Message(key, params));
    }

    void addAll(RamlViolations violations) {
        this.messages.addAll(violations.messages);
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public List<RamlViolationMessage> asList() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public Iterator<RamlViolationMessage> iterator() {
        return messages.iterator();
    }

    @Override
    public String toString() {
        return messages.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RamlViolations that = (RamlViolations) o;
        return messages.equals(that.messages);

    }

    @Override
    public int hashCode() {
        return messages.hashCode();
    }
}
