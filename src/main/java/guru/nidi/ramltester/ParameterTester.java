package guru.nidi.ramltester;

import org.raml.model.parameter.AbstractParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
class ParameterTester {
    private static final Pattern INTEGER = Pattern.compile("0|-?[1-9][0-9]*");
    private static final Pattern NUMBER = Pattern.compile("0|inf|-inf|nan|-?(((0?|[1-9][0-9]*)\\.[0-9]*[1-9])|([1-9][0-9]*))(e[-+]?[1-9][0-9]*)?");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlViolations violations;
    private final boolean acceptUndefined;

    ParameterTester(RamlViolations violations, boolean acceptUndefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
    }

    public void testParameters(Map<String, ? extends AbstractParam> params, Map<String, String[]> values, String description) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, String[]> entry : values.entrySet()) {
            final AbstractParam parameter = params.get(entry.getKey());
            final String d = description + " '" + entry.getKey() + "'";
            if (parameter == null) {
                violations.addIf(!acceptUndefined, d + " is not defined");
            } else {
                violations.addIf(!parameter.isRepeat() && entry.getValue().length > 1,
                        d + " is not repeat but found repeatedly in response");
                for (String value : entry.getValue()) {
                    testParameter(parameter, value, d);
                }
                found.add(entry.getKey());
            }
        }
        for (Map.Entry<String, ? extends AbstractParam> entry : params.entrySet()) {
            final String d = description + " '" + entry.getKey() + "'";
            violations.addIf(entry.getValue().isRequired() && !found.contains(entry.getKey()),
                    d + " is required but not found in response");
        }
    }

    public void testParameter(AbstractParam param, String value, String description) {
        String d = description + ": Value '" + value + "' ";
        switch (param.getType()) {
            case BOOLEAN:
                violations.addIf(!value.equals("true") && !value.equals("false"), d + "is not a valid boolean");
                break;
            case DATE:
                try {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
                    dateFormat.setLenient(false);
                    dateFormat.parse(value);
                } catch (ParseException e) {
                    violations.add(d + "is not a valid date");
                }
                break;
            case FILE:
                //TODO
                break;
            case INTEGER:
                if (INTEGER.matcher(value).matches()) {
                    testNumericLimits(param, new BigDecimal(value), d);
                } else {
                    violations.add(d + "is not a valid integer");
                }
                break;
            case NUMBER:
                if (NUMBER.matcher(value).matches()) {
                    if ((value.equals("inf") || value.equals("-inf") || value.equals("nan"))) {
                        violations.addIf(param.getMinimum() != null || param.getMaximum() != null, d + "is not inside any minimum/maximum");
                    } else {
                        testNumericLimits(param, new BigDecimal(value), d);
                    }
                } else {
                    violations.add(d + "is not a valid number");
                }
                break;
            case STRING:
                violations.addIf(param.getEnumeration() != null && !param.getEnumeration().contains(value),
                        d + "is not a member of enum '" + param.getEnumeration() + "'");
                try {
                    violations.addIf(param.getPattern() != null && !javaRegexOf(param.getPattern()).matcher(value).matches(),
                            d + "does not match pattern '" + param.getPattern() + "'");
                } catch (PatternSyntaxException e) {
                    log.warn("Could not execute regex '" + param.getPattern(), e);
                }
                violations.addIf(param.getMinLength() != null && value.length() < param.getMinLength(),
                        d + "is shorter than minimum length " + param.getMinLength());
                violations.addIf(param.getMaxLength() != null && value.length() > param.getMaxLength(),
                        d + "is longer than maximum length " + param.getMaximum());
                break;
        }
    }

    private void testNumericLimits(AbstractParam param, BigDecimal value, String description) {
        violations.addIf(param.getMinimum() != null && param.getMinimum().compareTo(value) > 0,
                description + "is less than minimum " + param.getMinimum());
        violations.addIf(param.getMaximum() != null && param.getMaximum().compareTo(value) < 0,
                description + "is bigger than maximum " + param.getMaximum());
    }

    private Pattern javaRegexOf(String regex) {
        if (isDoubleQuoted(regex) || isSingleQuoted(regex)) {
            regex = regex.substring(1, regex.length() - 1);
        }
        int flags = 0;
        if (regex.startsWith("/")) {
            int pos = regex.lastIndexOf("/");
            if (pos >= regex.length() - 3) {
                String flagString = pos == regex.length() - 1 ? "" : regex.substring(pos + 1);
                regex = regex.substring(1, pos);
                regex = regex.replace("\\/", "/");
                if (flagString.contains("i")) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }
            }
        }
        return Pattern.compile(regex, flags);
    }

    private boolean isDoubleQuoted(String regex) {
        return regex.startsWith("\"") && regex.endsWith("\"");
    }

    private boolean isSingleQuoted(String regex) {
        return regex.startsWith("'") && regex.endsWith("'");
    }
}
