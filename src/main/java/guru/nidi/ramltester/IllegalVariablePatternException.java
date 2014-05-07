package guru.nidi.ramltester;

/**
 *
 */
public class IllegalVariablePatternException extends RuntimeException {
    private final String pattern;

    public IllegalVariablePatternException(String message, String pattern) {
        super(message);
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
