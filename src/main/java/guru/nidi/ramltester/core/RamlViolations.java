package guru.nidi.ramltester.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class RamlViolations implements Iterable<String> {
    private final List<String> violations;

    RamlViolations() {
        this.violations = new ArrayList<>();
    }

    void add(Message message) {
        violations.add(message.toString());
    }

    void add(String key, Object... params) {
        add(new Message(key, params));
    }

    void addAndThrow(String key, Object... params) {
        add(key, params);
        throw new RamlViolationException();
    }

    void addIf(boolean condition, Message message) {
        if (condition) {
            add(message);
        }
    }

    void addIf(boolean condition, String key, Object... params) {
        addIf(condition, new Message(key, params));
    }

    void addAndThrowIf(boolean condition, String key, Object... params) {
        if (condition) {
            addAndThrow(key, params);
        }
    }

    public int size() {
        return violations.size();
    }

    public boolean isEmpty() {
        return violations.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        return violations.iterator();
    }

    @Override
    public String toString() {
        return violations.toString();
    }
}
