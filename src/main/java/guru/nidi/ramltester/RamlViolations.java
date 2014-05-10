package guru.nidi.ramltester;

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

    void add(String violation) {
        violations.add(violation);
    }

    void addAndThrow(String violation) {
        violations.add(violation);
        throw new RamlViolationException();
    }

    void addIf(boolean condition, String violation) {
        if (condition) {
            violations.add(violation);
        }
    }

    void addAndThrowIf(boolean condition, String violation) {
        addIf(condition, violation);
        if (condition) {
            throw new RamlViolationException();
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
