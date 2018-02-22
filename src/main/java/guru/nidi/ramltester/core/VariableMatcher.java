/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.Values;

import java.util.*;

/**
 * Match variables against RFC6570 level 2.
 */
final class VariableMatcher {
    private int patternPos, valuePos;
    private final String pattern;
    private final String value;
    private final Values variables;

    public VariableMatcher(String pattern, String value) {
        this(pattern, value, 0, 0, new Values());
    }

    private VariableMatcher(String pattern, String value, int patternPos, int valuePos, Values variables) {
        this.pattern = pattern;
        this.value = value;
        this.patternPos = patternPos;
        this.valuePos = valuePos;
        this.variables = new Values(variables);
    }

    private VariableMatcher newMatcher() {
        return new VariableMatcher(pattern, value, patternPos, valuePos, variables);
    }

    private VariableMatcher var(String name, String value) {
        variables.addValue(name, value);
        return this;
    }

    public List<Match> match() {
        while (patternPos < pattern.length() && valuePos < value.length()) {
            if (pattern.charAt(patternPos) == '{') {
                final char operator = pattern.charAt(patternPos + 1);
                final boolean reserved = operator == '+';
                final boolean hashed = operator == '#';
                if (reserved || hashed) {
                    patternPos++;
                }
                final List<String> varNames = extractVarNames();
                final Character end = patternPos < pattern.length() ? pattern.charAt(patternPos) : null;
                return extractVarValues(varNames, reserved, hashed, end);
            } else {
                if (pattern.charAt(patternPos) != value.charAt(valuePos)) {
                    return Collections.emptyList();
                }
                patternPos++;
                valuePos++;
            }
        }
        return patternPos == pattern.length()
                ? Collections.singletonList(new Match(value.substring(valuePos), variables))
                : Collections.<Match>emptyList();
    }

    private List<String> extractVarNames() {
        final List<String> names = new ArrayList<>();
        do {
            patternPos++;
            final StringBuilder varName = new StringBuilder();
            while (patternPos < pattern.length() && pattern.charAt(patternPos) != '}') {
                varName.append(pattern.charAt(patternPos));
                patternPos++;
            }
            if (patternPos == pattern.length() && pattern.charAt(patternPos - 1) != '}') {
                throw new IllegalVariablePatternException("Unclosed variable " + varName, pattern);
            }
            patternPos++;
            names.add(varName.toString());
        } while (patternPos < pattern.length() && pattern.charAt(patternPos) == '{');
        return names;
    }

    private List<Match> extractVarValues(List<String> varNames, boolean reserved, boolean hashed, Character end) {
        final List<Match> matches = new ArrayList<>();
        final StringBuilder varValue = new StringBuilder();
        while (valuePos < value.length() && isOk(reserved || hashed, value.charAt(valuePos))) {
            if (end == null || value.charAt(valuePos) == end) {
                matchWithVar(varNames, varValue.toString(), hashed, newMatcher(), matches);
            }
            varValue.append(value.charAt(valuePos));
            valuePos++;
        }
        if (end == null || (valuePos < value.length() && value.charAt(valuePos) == end)) {
            matchWithVar(varNames, varValue.toString(), hashed, newMatcher(), matches);
        }
        return matches;
    }

    private void matchWithVar(List<String> names, String val, boolean hashed, VariableMatcher matcher, List<Match> matches) {
        if (names.size() > 1) {
            for (int i = 0; i <= val.length(); i++) {
                matchWithVar(names.subList(1, names.size()), val.substring(i), hashed, newMatcher().var(names.get(0), val.substring(0, i)), matches);
            }
        }
        if (names.size() == 1) {
            if (hashed) {
                if (val.startsWith("#")) {
                    matches.addAll(matcher.var(names.get(0), val.substring(1)).match());
                }
            } else {
                matches.addAll(matcher.var(names.get(0), val).match());
            }
        }
    }

    private static boolean isOk(boolean reserved, char c) {
        return isUnreserved(c) || (reserved && isReserved(c));
    }

    private static boolean isUnreserved(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '.' || c == '_' || c == '~';
    }

    private static boolean isReserved(char c) {
        return ":/?#[]@!$&'()*+,;=".indexOf(c) >= 0;
    }

    public static class Match {
        public final String suffix;
        public final Values variables;

        Match(String suffix, Values variables) {
            this.suffix = suffix;
            this.variables = variables;
        }

        public boolean isComplete() {
            return suffix.length() == 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Match match = (Match) o;

            if (!suffix.equals(match.suffix)) {
                return false;
            }
            return variables.equals(match.variables);
        }

        @Override
        public int hashCode() {
            int result = suffix.hashCode();
            result = 31 * result + variables.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Match{"
                    + "suffix='" + suffix + '\''
                    + ", variables=" + variables
                    + '}';
        }
    }

}

