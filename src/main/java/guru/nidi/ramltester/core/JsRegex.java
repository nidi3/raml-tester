package guru.nidi.ramltester.core;


import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class JsRegex {
    private static final String MATCHES_IN_JS
            = "var matches = function(input, re, flags){"
            + "    var r = flags ? new RegExp(re,flags) : new RegExp(re);"
            + "    return r.test(input);"
            + "};";

    public static class InvalidRegexException extends RuntimeException {
        public InvalidRegexException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final ScriptEngine ENGINE;

    private JsRegex() {
    }

    static {
        try {
            ENGINE = new ScriptEngineManager().getEngineByExtension("js");
            ENGINE.eval(MATCHES_IN_JS);
        } catch (ScriptException e) {
            throw new AssertionError("Could not initialize js engine", e);
        }
    }

    public static boolean matches(String input, String regex) throws InvalidRegexException {
        if (isDoubleQuoted(regex) || isSingleQuoted(regex)) {
            regex = regex.substring(1, regex.length() - 1);
        }
        String flags = null;
        if (regex.startsWith("/")) {
            int pos = regex.lastIndexOf("/");
            if (pos >= regex.length() - 3) {
                flags = pos == regex.length() - 1 ? "" : regex.substring(pos + 1);
                regex = regex.substring(1, pos).replace("\\/", "/");
            }
        }
        return matches(input, regex, flags);
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
