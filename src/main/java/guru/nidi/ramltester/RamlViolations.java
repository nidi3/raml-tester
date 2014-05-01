package guru.nidi.ramltester;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RamlViolations {
    private final List<String> violations;

    RamlViolations() {
        this.violations = new ArrayList<>();
    }

    void addViolation(String violation) {
        violations.add(violation);
    }

    void addViolation(boolean condition, String violation) {
        if (condition) {
            violations.add(violation);
        }
    }

    void addViolationAndThrow(boolean condition, String violation) {
        addViolation(condition, violation);
        if (condition) {
            throw new RamlViolationException();
        }
    }

    public List<String> getViolations() {
        return violations;
    }

    @Override
    public String toString() {
        return violations.toString();
    }
}
