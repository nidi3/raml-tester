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
package guru.nidi.ramltester.model;


import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

final class JsRegex {
    private static final String MATCHES_IN_JS
            = "var matches = function(input, re, flags){"
            + "    var r = flags ? new RegExp(re,flags) : new RegExp(re);"
            + "    return r.test(input);"
            + "};";

    private static final ScriptEngine ENGINE;

    public static class InvalidRegexException extends RuntimeException {
        public InvalidRegexException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private JsRegex() {
    }

    static {
        try {
            ENGINE = new ScriptEngineManager().getEngineByExtension("js");
            ENGINE.eval(MATCHES_IN_JS);
        } catch (ScriptException e) {
            throw new AssertionError(e);
        }
    }

    public static boolean matches(String input, String regex) {
        final String unquoted = (isDoubleQuoted(regex) || isSingleQuoted(regex))
                ? regex.substring(1, regex.length() - 1)
                : regex;
        if (unquoted.startsWith("/")) {
            final int pos = unquoted.lastIndexOf('/');
            if (pos >= unquoted.length() - 3) {
                final String flags = pos == unquoted.length() - 1 ? "" : unquoted.substring(pos + 1);
                final String unslashed = unquoted.substring(1, pos).replace("\\/", "/");
                return matches(input, unslashed, flags);
            }
        }
        return matches(input, unquoted, null);
    }

    public static boolean matches(String input, String regex, String flags) {
        try {
            return (boolean) ((Invocable) ENGINE).invokeFunction("matches", input, regex, flags);
        } catch (Exception e) {
            Throwable t = e;
            while (t.getMessage() == null && t.getCause() != null) {
                t = t.getCause();
            }
            throw new InvalidRegexException(t.getMessage(), t);
        }
    }

    private static boolean isDoubleQuoted(String regex) {
        return regex.startsWith("\"") && regex.endsWith("\"");
    }

    private static boolean isSingleQuoted(String regex) {
        return regex.startsWith("'") && regex.endsWith("'");
    }
}
