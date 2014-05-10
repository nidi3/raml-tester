package guru.nidi.ramltester;

/**
 *
 */
public class RamlViolationReport {
    private final RamlViolations requestViolations = new RamlViolations();
    private final RamlViolations responseViolations = new RamlViolations();

    public RamlViolationReport() {
    }

    public boolean isEmpty() {
        return requestViolations.isEmpty() && responseViolations.isEmpty();
    }

    @Override
    public String toString() {
        return "RamlViolationReport{" +
                "requestViolations=" + requestViolations +
                ", responseViolations=" + responseViolations +
                '}';
    }

    public RamlViolations getRequestViolations() {
        return requestViolations;
    }

    public RamlViolations getResponseViolations() {
        return responseViolations;
    }
}
