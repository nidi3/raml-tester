package guru.nidi.ramltester;

import org.raml.model.*;

import java.util.Map;

/**
 *
 */
class RamlTestRunner {
    private final Raml raml;
    private final SchemaValidator schemaValidator;
    private final RamlViolations violations;

    public RamlTestRunner(RamlDefinition ramlDefinition) {
        this.raml = ramlDefinition.getRaml();
        this.schemaValidator = ramlDefinition.getSchemaValidator();
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
        final String resourcePath = findResourcePath(request.getRequestUrl());
        Resource resource = raml.getResource(resourcePath);
        violations.addViolationAndThrow(resource == null, "Resource '" + resourcePath + "' not defined in raml " + raml.getTitle());
        Action action = resource.getAction(request.getMethod());
        violations.addViolationAndThrow(action == null, "Action " + request.getMethod() + " not defined on resource " + resource);
        new ParameterTester(violations, false)
                .testParameters(action.getQueryParameters(), request.getParameterMap(), "On action " + action + ", query parameter");
        return action;
    }

    private String findResourcePath(String requestUrl) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(requestUrl);
        final UriComponents ramlUri = UriComponents.fromHttpUrl(raml.getBaseUri());
        if (raml.getProtocols() != null && !raml.getProtocols().isEmpty()) {
            violations.addViolation(!raml.getProtocols().contains(protocolOf(requestUri.getScheme())),
                    "Protocol " + requestUri.getScheme() + " not defined in raml");
        } else {
            violations.addViolation(!ramlUri.getScheme().equalsIgnoreCase(requestUri.getScheme()),
                    "Protocol " + requestUri.getScheme() + " not defined in raml");
        }
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            violations.addViolationAndThrow("Request URL " + requestUrl + " does not match base URI " + raml.getBaseUri());
        }
        final ParameterTester parameterTester = new ParameterTester(violations, true);
        parameterTester.testParameters(raml.getBaseUriParameters(), hostMatch.getVariables(), "BaseUri Parameter");
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            violations.addViolationAndThrow("Request URL " + requestUrl + " does not match base URI " + raml.getBaseUri());
        }
        parameterTester.testParameters(raml.getBaseUriParameters(), pathMatch.getVariables(), "BaseUri Parameter");
        return pathMatch.getSuffix();
    }

    private Protocol protocolOf(String s) {
        if (s.equalsIgnoreCase("http")) {
            return Protocol.HTTP;
        }
        if (s.equalsIgnoreCase("https")) {
            return Protocol.HTTPS;
        }
        return null;
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
        try {
            MediaType targetType = MediaType.valueOf(toFind);
            for (Map.Entry<String, MimeType> entry : bodies.entrySet()) {
                if (targetType.isCompatibleWith(MediaType.valueOf(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        } catch (InvalidMimeTypeException e) {
            violations.addViolation("Illegal Media type '" + e.getMimeType() + "'");
        }
        return null;
    }

}
