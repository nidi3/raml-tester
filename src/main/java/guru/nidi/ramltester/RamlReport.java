package guru.nidi.ramltester;

/**
 *
 */
public class RamlReport {
    private final RamlViolations requestViolations = new RamlViolations();
    private final RamlViolations responseViolations = new RamlViolations();

    public RamlReport() {
    }

    public boolean isEmpty() {
        return requestViolations.isEmpty() && responseViolations.isEmpty();
    }

    @Override
    public String toString() {
        return "RamlReport{" +
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
