package guru.nidi.ramltester;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class VariableMatcher {
    private final boolean matches;
    private final boolean completeMatch;
    private final String suffix;
    private final Map<String, String[]> variables;

    private VariableMatcher(boolean matches, boolean completeMatch, String suffix, Map<String, String[]> variables) {
        this.matches = matches;
        this.completeMatch = completeMatch;
        this.suffix = suffix;
        this.variables = variables;
    }

    public static VariableMatcher match(String pattern, String value) {
        Map<String, String[]> variables = new HashMap<>();
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
                final String[] newValues = appendValue(variables.get(varName.toString()), varValue.toString());
                variables.put(varName.toString(), newValues);
            } else {
                if (pattern.charAt(patternPos) != value.charAt(valuePos)) {
                    return new VariableMatcher(false, false, "", Collections.<String, String[]>emptyMap());
                }
                patternPos++;
                valuePos++;
            }
        }
        final boolean match = patternPos == pattern.length();
        return new VariableMatcher(match, match && valuePos == value.length(), value.substring(valuePos), variables);
    }

    private static String[] appendValue(String[] values, String value) {
        final String[] newValues;
        if (values == null) {
            newValues = new String[]{value};
        } else {
            newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
        }
        return newValues;
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

    public Map<String, String[]> getVariables() {
        return variables;
    }
}
