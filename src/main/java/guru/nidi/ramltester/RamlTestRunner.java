package guru.nidi.ramltester;

import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.QueryParameter;

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
class RamlTestRunner {
    private static final Pattern INTEGER = Pattern.compile("0|-?[1-9][0-9]*");
    private static final Pattern NUMBER = Pattern.compile("0|inf|-inf|nan|-?(((0?|[1-9][0-9]*)\\.[0-9]*[1-9])|([1-9][0-9]*))(e[-+]?[1-9][0-9]*)?");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private final Raml raml;
    private final SchemaValidator schemaValidator;
    private final RamlViolations violations;

    public RamlTestRunner(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator;
        violations = new RamlViolations();
    }

    public RamlViolations getViolations() {
        return violations;
    }

    public void test(HttpRequest request, HttpResponse response) {
        try {
            Action action = testRequest(request);
            testResponse(action, response);
        } catch (RamlViolationException e) {
            //ignore, results are in violations
        }
    }

    public Action testRequest(HttpRequest request) {
        Resource resource = raml.getResource(request.getRequestURI());
        violations.addViolationAndThrow(resource == null, "Resource " + request.getRequestURI() + " not defined in raml" + raml);
        Action action = resource.getAction(request.getMethod());
        violations.addViolationAndThrow(action == null, "Action " + request.getMethod() + " not defined on resource " + resource);
        testParameters(action, request);
        return action;
    }

    private void testParameters(Action action, HttpRequest request) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            final QueryParameter queryParameter = action.getQueryParameters().get(entry.getKey());
            final String description = "Query parameter '" + entry.getKey() + "' ";
            if (queryParameter == null) {
                violations.addViolation(description + "not defined on action " + action);
            } else {
                violations.addViolation(!queryParameter.isRepeat() && entry.getValue().length > 1,
                        description + "on action " + action + " is not repeat but found repeatedly in response");
                for (String value : entry.getValue()) {
                    testParameter(queryParameter, value, description);
                }
                found.add(entry.getKey());
            }
        }
        for (Map.Entry<String, QueryParameter> entry : action.getQueryParameters().entrySet()) {
            final String description = "Query parameter '" + entry.getKey() + "' ";
            violations.addViolation(entry.getValue().isRequired() && !found.contains(entry.getKey()),
                    description + "on action " + action + " is required but not found in response");
        }
    }

    public void testResponse(Action action, HttpResponse response) {
        Response res = action.getResponses().get("" + response.getStatus());
        violations.addViolationAndThrow(res == null, "Response code " + response.getStatus() + " not defined on action " + action);
        violations.addViolationAndThrow(response.getContentType() == null, "Response has no Content-Type header");
        final Map<String, MimeType> bodies = res.getBody();
        if (bodies != null) {
            MimeType mimeType = findMatchingMimeType(bodies, response.getContentType());
            violations.addViolationAndThrow(mimeType == null, "Media type '" + response.getContentType() + "' not defined on response " + res);
            String schema = mimeType.getSchema();
            if (schema != null) {
                if (!schema.trim().startsWith("{")) {
                    schema = raml.getConsolidatedSchemas().get(mimeType.getSchema());
                    violations.addViolationAndThrow(schema == null, "Schema '" + mimeType.getSchema() + "' referenced but not defined");
                }
                schemaValidator.validate(violations, response.getContentAsString(), schema);
            }
        }
    }

    private MimeType findMatchingMimeType(Map<String, MimeType> bodies, String toFind) {
        org.springframework.util.MimeType targetType = org.springframework.util.MimeType.valueOf(toFind);
        for (Map.Entry<String, MimeType> entry : bodies.entrySet()) {
            if (org.springframework.util.MimeType.valueOf(entry.getKey()).isCompatibleWith(targetType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void testParameter(AbstractParam param, String value, String description) {
        String d = description + ": Value '" + value + "' ";
        switch (param.getType()) {
            case BOOLEAN:
                violations.addViolation(!value.equals("true") && !value.equals("false"), d + "is not a valid boolean");
                break;
            case DATE:
                try {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
                    dateFormat.setLenient(false);
                    dateFormat.parse(value);
                } catch (ParseException e) {
                    violations.addViolation(d + "is not a valid date");
                }
                break;
            case FILE:
                //TODO
                break;
            case INTEGER:
                if (INTEGER.matcher(value).matches()) {
                    testNumericLimits(param, new BigDecimal(value), d);
                } else {
                    violations.addViolation(d + "is not a valid integer");
                }
                break;
            case NUMBER:
                if (NUMBER.matcher(value).matches()) {
                    if ((value.equals("inf") || value.equals("-inf") || value.equals("nan"))) {
                        violations.addViolation(param.getMinimum() != null || param.getMaximum() != null, d + "is not inside any minimum/maximum");
                    } else {
                        testNumericLimits(param, new BigDecimal(value), d);
                    }
                } else {
                    violations.addViolation(d + "is not a valid number");
                }
                break;
            case STRING:
                violations.addViolation(param.getEnumeration() != null && !param.getEnumeration().contains(value),
                        d + "is not a member of enum '" + param.getEnumeration() + "'");
                try {
                    violations.addViolation(param.getPattern() != null && !javaRegexOf(param.getPattern()).matcher(value).matches(),
                            d + "does not match pattern '" + param.getPattern() + "'");
                } catch (PatternSyntaxException e) {
                    //TODO log
                }
                violations.addViolation(param.getMinLength() != null && value.length() < param.getMinLength(),
                        d + "is shorter than minimum length " + param.getMinLength());
                violations.addViolation(param.getMaxLength() != null && value.length() > param.getMaxLength(),
                        d + "is longer than maximum length " + param.getMaximum());
                break;
        }
    }

    private void testNumericLimits(AbstractParam param, BigDecimal value, String description) {
        violations.addViolation(param.getMinimum() != null && param.getMinimum().compareTo(value) > 0,
                description + "is less than minimum " + param.getMinimum());
        violations.addViolation(param.getMaximum() != null && param.getMaximum().compareTo(value) < 0,
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
