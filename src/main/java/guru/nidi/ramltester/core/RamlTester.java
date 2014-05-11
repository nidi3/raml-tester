package guru.nidi.ramltester.core;

import guru.nidi.ramltester.util.ParameterValues;
import guru.nidi.ramltester.util.UriComponents;
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
public class RamlTester {
    private final Raml raml;
    private final SchemaValidator schemaValidator;
    private RamlReport report;
    private RamlViolations requestViolations, responseViolations;

    public RamlTester(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator;
    }

    public RamlReport test(RamlRequest request, RamlResponse response) {
        report = new RamlReport();
        requestViolations = report.getRequestViolations();
        responseViolations = report.getResponseViolations();
        try {
            Action action = testRequest(request);
            testResponse(action, response);
        } catch (RamlViolationException e) {
            //ignore, results are in report
        }
        return report;
    }

    public Action testRequest(RamlRequest request) {
        final String resourcePath = findResourcePath(request.getRequestUrl());
        Resource resource = findResource(resourcePath);
        requestViolations.addAndThrowIf(resource == null, "resource.undefined", resourcePath);
        Action action = resource.getAction(request.getMethod());
        requestViolations.addAndThrowIf(action == null, "action.undefined", request.getMethod(), resource);
        new ParameterTester(requestViolations, false)
                .testParameters(action.getQueryParameters(), request.getParameterMap(), new Message("queryParam", action));
        return action;
    }

    private String findResourcePath(String requestUrl) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(requestUrl);
        final UriComponents ramlUri = UriComponents.fromHttpUrl(raml.getBaseUri());
        if (raml.getProtocols() != null && !raml.getProtocols().isEmpty()) {
            requestViolations.addIf(!raml.getProtocols().contains(protocolOf(requestUri.getScheme())),
                    "protocol.undefined", requestUri.getScheme());
        } else {
            requestViolations.addIf(!ramlUri.getScheme().equalsIgnoreCase(requestUri.getScheme()),
                    "protocol.undefined", requestUri.getScheme());
        }
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            requestViolations.addAndThrow("baseUri.unmatched", requestUrl, raml.getBaseUri());
        }
        final ParameterTester parameterTester = new ParameterTester(requestViolations, true);
        parameterTester.testParameters(raml.getBaseUriParameters(), hostMatch.getVariables().getValues(), new Message("baseUriParam"));
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            requestViolations.addAndThrow("baseUri.unmatched", requestUrl, raml.getBaseUri());
        }
        parameterTester.testParameters(raml.getBaseUriParameters(), pathMatch.getVariables().getValues(), new Message("baseUriParam"));
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
        final ParameterTester parameterTester = new ParameterTester(requestViolations, true);
        for (Map.Entry<String, String[]> entry : parameterValues.getValues().entrySet()) {
            final AbstractParam uriParam = findUriParam(entry.getKey(), resource);
            if (uriParam != null) {
                parameterTester.testParameter(uriParam, entry.getValue()[0], new Message("uriParam", entry.getKey(), resource));
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

    public void testResponse(Action action, RamlResponse response) {
        Response res = action.getResponses().get("" + response.getStatus());
        responseViolations.addAndThrowIf(res == null, "responseCode.undefined", response.getStatus(), action);
        final Map<String, MimeType> bodies = res.getBody();
        if (bodies == null || bodies.isEmpty()) {
            responseViolations.addIf(hasContent(response), "responseBody.superfluous", action, response.getStatus());
        } else {
            if (response.getContentType() == null) {
                responseViolations.addAndThrowIf(hasContent(response) || !existSchemalessBody(bodies), "contentType.missing");
            } else {
                MimeType mimeType = findMatchingMimeType(bodies, response.getContentType());
                responseViolations.addAndThrowIf(mimeType == null, "mediaType.undefined", response.getContentType(), action, response.getStatus());
                String schema = mimeType.getSchema();
                if (schema != null) {
                    if (!schema.trim().startsWith("{")) {
                        schema = raml.getConsolidatedSchemas().get(mimeType.getSchema());
                        responseViolations.addAndThrowIf(schema == null, "schema.missing", mimeType.getSchema(), action, response.getStatus(), mimeType);
                    }
                    schemaValidator.validate(response.getContentAsString(), schema, responseViolations, new Message("responseBody.mismatch", action, response.getStatus(), mimeType));
                }
            }
        }
    }

    private boolean hasContent(RamlResponse response) {
        return response.getContentAsString() != null && response.getContentAsString().length() > 0;
    }

    private boolean existSchemalessBody(Map<String, MimeType> bodies) {
        for (MimeType mimeType : bodies.values()) {
            if (mimeType.getSchema() == null) {
                return true;
            }
        }
        return false;
    }

    private MimeType findMatchingMimeType(Map<String, MimeType> bodies, String toFind) {
        try {
            MediaType targetType = MediaType.valueOf(toFind);
            for (Map.Entry<String, MimeType> entry : bodies.entrySet()) {
                if (targetType.isCompatibleWith(MediaType.valueOf(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        } catch (InvalidMediaTypeException e) {
            responseViolations.add("mediaType.illegal", e.getMimeType());
        }
        return null;
    }

}
