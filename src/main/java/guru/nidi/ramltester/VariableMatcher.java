package guru.nidi.ramltester;

import guru.nidi.ramltester.util.ParameterValues;

/**
 *
 */
class VariableMatcher {
    private final boolean matches;
    private final boolean completeMatch;
    private final String suffix;
    private final ParameterValues variables;

    private VariableMatcher(boolean matches, boolean completeMatch, String suffix, ParameterValues variables) {
        this.matches = matches;
        this.completeMatch = completeMatch;
        this.suffix = suffix;
        this.variables = variables;
    }

    public static VariableMatcher match(String pattern, String value) {
        final ParameterValues variables = new ParameterValues();
        int patternPos = 0, valuePos = 0;
        while (patternPos < pattern.length() && valuePos < value.length()) {
            if (pattern.charAt(patternPos) == '{') {
                StringBuilder varName = new StringBuilder();
                patternPos++;
                while (patternPos < pattern.length() && pattern.charAt(patternPos) != '}') {
                    varName.append(pattern.charAt(patternPos));
                    patternPos++;
                }
                if (patternPos == pattern.length() && pattern.charAt(patternPos - 1) != '}') {
                    throw new IllegalVariablePatternException("Unclosed variable " + varName, pattern);
                }
                patternPos++;
                char next = patternPos < pattern.length() ? pattern.charAt(patternPos) : '/';
                StringBuilder varValue = new StringBuilder();
                while (valuePos < value.length() && value.charAt(valuePos) != next) {
                    varValue.append(value.charAt(valuePos));
                    valuePos++;
                }
                variables.addValue(varName.toString(), varValue.toString());
            } else {
                if (pattern.charAt(patternPos) != value.charAt(valuePos)) {
                    return new VariableMatcher(false, false, "", new ParameterValues());
                }
                patternPos++;
                valuePos++;
            }
        }
        final boolean match = patternPos == pattern.length();
        return new VariableMatcher(match, match && valuePos == value.length(), value.substring(valuePos), variables);
    }


    public boolean isMatch() {
        return matches;
    }

    public boolean isCompleteMatch() {
        return completeMatch;
    }

    public String getSuffix() {
        return suffix;
    }

    public ParameterValues getVariables() {
        return variables;
    }
}
