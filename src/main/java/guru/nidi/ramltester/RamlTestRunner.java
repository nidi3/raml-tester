package guru.nidi.ramltester;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.raml.model.*;
import org.raml.model.parameter.QueryParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class RamlTestRunner {
    private final Raml raml;
    private final MatcherProvider<String> schemaValidatorProvider;
    private final RamlViolations violations;

    public RamlTestRunner(Raml raml, MatcherProvider<String> schemaValidatorProvider) {
        this.raml = raml;
        this.schemaValidatorProvider = schemaValidatorProvider;
        violations = new RamlViolations();
    }

    public RamlViolations getViolations() {
        return violations;
    }

    public void test(MockHttpServletRequest request, MockHttpServletResponse response) {
        try {
            Action action = testRequest(request);
            testResponse(action, response);
        } catch (RamlViolationException e) {
            //ignore, results are in violations
        }
    }

    public Action testRequest(MockHttpServletRequest request) {
        Resource resource = raml.getResource(request.getRequestURI());
        violations.addViolationAndThrow(resource == null, "Resource " + request.getRequestURI() + " not defined in raml" + raml);
        Action action = resource.getAction(request.getMethod());
        violations.addViolationAndThrow(action == null, "Action " + request.getMethod() + " not defined on resource " + resource);
        testParameters(action, request);
        return action;
    }

    private void testParameters(Action action, MockHttpServletRequest request) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            final QueryParameter queryParameter = action.getQueryParameters().get(entry.getKey());
            violations.addViolation(queryParameter == null,
                    "Query parameter '" + entry.getKey() + "' not defined on action " + action);
            violations.addViolation(queryParameter != null && !queryParameter.isRepeat() && entry.getValue().length > 1,
                    "Query parameter '" + entry.getKey() + "' on action " + action + " is not repeat but found repeatedly in response");
            found.add(entry.getKey());
        }
        for (Map.Entry<String, QueryParameter> entry : action.getQueryParameters().entrySet()) {
            violations.addViolation(entry.getValue().isRequired() && !found.contains(entry.getKey()),
                    "Query parameter '" + entry.getKey() + "' on action " + action + " is required but not found in response");
        }
    }

    public void testResponse(Action action, MockHttpServletResponse response) {
        Response res = action.getResponses().get("" + response.getStatus());
        violations.addViolationAndThrow(res == null, "Response code " + response.getStatus() + " not defined on action " + action);
        violations.addViolationAndThrow(response.getContentType() == null, "Response has no Content-Type header");
        MimeType mimeType = findMatchingMimeType(res, response.getContentType());
        violations.addViolationAndThrow(mimeType == null, "Mime type '" + response.getContentType() + "' not defined on response " + res);
        String schema = mimeType.getSchema();
        if (schema != null) {
            if (!schema.trim().startsWith("{")) {
                schema = raml.getConsolidatedSchemas().get(mimeType.getSchema());
                violations.addViolationAndThrow(schema == null, "Schema '" + mimeType.getSchema() + "' referenced but not defined");
            }
            try {
                testResponseContent(response.getContentAsString(), schema);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void testResponseContent(String content, String schema) {
        final Matcher<String> matcher = schemaValidatorProvider.getMatcher(schema);
        if (!matcher.matches(content)) {
            Description description = new StringDescription();
            description.appendText("Response content ");
            description.appendValue(content);
            description.appendText(" does not match schema: ");
            description.appendDescriptionOf(matcher);
            violations.addViolation(description.toString());
        }
    }

    private MimeType findMatchingMimeType(Response res, String toFind) {
        org.springframework.util.MimeType targetType = org.springframework.util.MimeType.valueOf(toFind);
        for (Map.Entry<String, MimeType> entry : res.getBody().entrySet()) {
            if (org.springframework.util.MimeType.valueOf(entry.getKey()).isCompatibleWith(targetType)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
