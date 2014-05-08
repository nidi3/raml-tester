package guru.nidi.ramltester;

import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        Resource resource = findResource(resourcePath);
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
        parameterTester.testParameters(raml.getBaseUriParameters(), hostMatch.getVariables().getValues(), "BaseUri Parameter");
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            violations.addViolationAndThrow("Request URL " + requestUrl + " does not match base URI " + raml.getBaseUri());
        }
        parameterTester.testParameters(raml.getBaseUriParameters(), pathMatch.getVariables().getValues(), "BaseUri Parameter");
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

    private Resource findResource(String resourcePath) {
        final ParameterValues parameterValues = new ParameterValues();
        final Resource resource = findResource(resourcePath, raml.getResources(), parameterValues);
        if (resource == null) {
            return null;
        }
        final ParameterTester parameterTester = new ParameterTester(violations, true);
        for (Map.Entry<String, String[]> entry : parameterValues.getValues().entrySet()) {
            final AbstractParam uriParam = findUriParam(entry.getKey(), resource);
            if (uriParam != null) {
                parameterTester.testParameter(uriParam, entry.getValue()[0], "URI parameter '" + entry.getKey() + "' on resource " + resource);
            }
        }
        return resource;
    }

    private AbstractParam findUriParam(String uriParam, Resource resource) {
        final UriParameter param = resource.getUriParameters().get(uriParam);
        if (param != null) {
            return param;
        }
        if (resource.getParentResource() != null) {
            return findUriParam(uriParam, resource.getParentResource());
        }
        return null;
    }

    private Resource findResource(String resourcePath, Map<String, Resource> resources, ParameterValues parameterValues) {
        List<ResourceMatch> matches = new ArrayList<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            final VariableMatcher pathMatch = VariableMatcher.match(entry.getKey(), resourcePath);
            if (pathMatch.isMatch()) {
                matches.add(new ResourceMatch(pathMatch, entry.getValue()));
            }
        }
        Collections.sort(matches);
        for (ResourceMatch match : matches) {
            if (match.match.isCompleteMatch()) {
                parameterValues.addValues(match.match.getVariables());
                return match.resource;
            }
            if (match.match.isMatch()) {
                parameterValues.addValues(match.match.getVariables());
                return findResource(match.match.getSuffix(), match.resource.getResources(), parameterValues);
            }

        }
        return null;
    }

    private static class ResourceMatch implements Comparable<ResourceMatch> {
        private final VariableMatcher match;
        private final Resource resource;

        private ResourceMatch(VariableMatcher match, Resource resource) {
            this.match = match;
            this.resource = resource;
        }

        @Override
        public int compareTo(ResourceMatch o) {
            return match.getVariables().size() - o.match.getVariables().size();
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
