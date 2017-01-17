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

import guru.nidi.ramltester.model.Values;

/**
 * Match variables against RFC6570 level 2.
 */
final class VariableMatcher {
    private int patternPos, valuePos;
    private final String pattern, value;

    public VariableMatcher(String pattern, String value) {
        this.pattern = pattern;
        this.value = value;
    }

    public Match match() {
        final Values variables = new Values();
        while (patternPos < pattern.length() && valuePos < value.length()) {
            if (pattern.charAt(patternPos) == '{') {
                patternPos++;
                final char operator = pattern.charAt(patternPos);
                final boolean reserved = operator == '+';
                final boolean hashed = operator == '#';
                if (reserved || hashed) {
                    patternPos++;
                }
                final String varName = extractVarName();
                final Character end = patternPos < pattern.length() ? pattern.charAt(patternPos) : null;
                final String varValue = extractVarValue(reserved || hashed, end);
                if (hashed) {
                    if (varValue.startsWith("#")) {
                        variables.addValue(varName, varValue.substring(1));
                    } else {
                        return new Match(false, false, "", new Values());
                    }
                } else {
                    variables.addValue(varName, varValue);
                }
            } else {
                if (pattern.charAt(patternPos) != value.charAt(valuePos)) {
                    return new Match(false, false, "", new Values());
                }
                patternPos++;
                valuePos++;
            }
        }
        final boolean match = patternPos == pattern.length();
        return new Match(match, match && valuePos == value.length(), value.substring(valuePos), variables);
    }

    private String extractVarName() {
        final StringBuilder varName = new StringBuilder();
        while (patternPos < pattern.length() && pattern.charAt(patternPos) != '}') {
            varName.append(pattern.charAt(patternPos));
            patternPos++;
        }
        if (patternPos == pattern.length() && pattern.charAt(patternPos - 1) != '}') {
            throw new IllegalVariablePatternException("Unclosed variable " + varName, pattern);
        }
        patternPos++;
        return varName.toString();
    }

    private String extractVarValue(boolean reserved, Character end) {
        final StringBuilder varValue = new StringBuilder();
        while (valuePos < value.length() && isOk(reserved, value.charAt(valuePos)) && (end == null || value.charAt(valuePos) != end)) {
            varValue.append(value.charAt(valuePos));
            valuePos++;
        }
        return varValue.toString();
    }

    private static boolean isOk(boolean reserved, char c) {
        return reserved ? isUnreserved(c) || isReserved(c) : isUnreserved(c);
    }

    private static boolean isUnreserved(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '.' || c == '_' || c == '~';
    }

    private static boolean isReserved(char c) {
        return ":/?#[]@!$&'()*+,;=".indexOf(c) >= 0;
    }

    public static class Match {
        public final boolean matches;
        public final boolean completeMatch;
        public final String suffix;
        public final Values variables;

        private Match(boolean matches, boolean completeMatch, String suffix, Values variables) {
            this.matches = matches;
            this.completeMatch = completeMatch;
            this.suffix = suffix;
            this.variables = variables;
        }
    }

}
