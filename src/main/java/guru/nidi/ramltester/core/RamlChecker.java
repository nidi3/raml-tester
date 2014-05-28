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

import guru.nidi.ramltester.util.UriComponents;
import guru.nidi.ramltester.util.Values;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.UriParameter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 *
 */
public class RamlChecker {
    private final static class DefaultHeaders {
        private static final Set<String>
                REQUEST = new HashSet<>(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language", "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length", "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma", "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning")),
                RESPONSE = new HashSet<>(Arrays.asList("access-control-allow-origin", "accept-ranges", "age", "allow", "cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-disposition", "content-range", "content-type", "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma", "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status", "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via", "warning", "www-authenticate", "x-frame-options"));
    }

    private static final MediaType FORM_ENCODED = MediaType.valueOf("application/x-www-form-urlencoded");
    private static final MediaType MULTIPART = MediaType.valueOf("multipart/form-data");

    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final String baseUri;
    private RamlReport report;
    private RamlViolations requestViolations, responseViolations;

    public RamlChecker(Raml raml, List<SchemaValidator> schemaValidators, String baseUri) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.baseUri = baseUri;
    }

    public RamlReport check(RamlRequest request, RamlResponse response) {
        report = new RamlReport();
        requestViolations = report.getRequestViolations();
        responseViolations = report.getResponseViolations();
        try {
            Action action = checkRequestAndFindAction(request);
            if (response != null) {
                checkResponse(action, response);
            }
        } catch (RamlViolationException e) {
            //ignore, results are in report
        }
        return report;
    }

    public RamlReport check(RamlRequest request) {
        return check(request, null);
    }

    public Action checkRequestAndFindAction(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(request.getRequestUrl(baseUri));
        final UriComponents ramlUri = UriComponents.fromHttpUrl(raml.getBaseUri());

        checkProtocol(requestUri, ramlUri);

        final VariableMatcher hostMatch = getHostMatch(requestUri, ramlUri);
        final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);

        Resource resource = findResource(pathMatch.getSuffix());
        Action action = findAction(resource, request.getMethod());

        checkBaseUriParameters(hostMatch, pathMatch, action);
        checkQueryParameters(request.getQueryValues(), action);
        checkRequestHeaderParameters(request.getHeaderValues(), action);

        checkFormRequestBody(request, action);
        checkBody(requestViolations, action, request, action.getBody(), "");
        return action;
    }

    private void checkFormRequestBody(RamlRequest request, Action action) {
        if (action.getBody() != null) {
            for (Map.Entry<String, MimeType> entry : action.getBody().entrySet()) {
                final MediaType mediaType = MediaType.valueOf(entry.getKey());
                if (mediaType.isCompatibleWith(FORM_ENCODED) || mediaType.isCompatibleWith(MULTIPART)) {
                    checkFormParameters(request, action, entry.getValue());
                }
            }
        }
    }

    private void checkFormParameters(RamlRequest request, Action action, MimeType mimeType) {
        if (mimeType.getSchema() != null) {
            requestViolations.add("schema.superfluous", action, mimeType);
        }
        final Map<String, List<FormParameter>> formParameters = mimeType.getFormParameters();
        if (formParameters == null) {
            requestViolations.add("formParameters.missing", action, mimeType);
        } else {
            checkFormParametersValues(request, action, formParameters);
        }
    }

    private void checkFormParametersValues(RamlRequest request, Action action, Map formParameters) {
        try {
            final Values values = decodeFormParameters(request.getContent());
            new ParameterChecker(requestViolations)
                    .checkListParameters((Map) formParameters, values, new Message("formParam", action));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private Values decodeFormParameters(String content) throws UnsupportedEncodingException {
        final Values values = new Values();
        if (content != null) {
            for (String param : content.split("&")) {
                final String[] parts = param.split("=");
                final String key = URLDecoder.decode(parts[0], "utf-8");
                final String value = URLDecoder.decode(parts[1], "utf-8");
                values.addValue(key, value);
            }
        }
        return values;
    }

    private void checkQueryParameters(Values values, Action action) {
        new ParameterChecker(requestViolations)
                .checkParameters(action.getQueryParameters(), values, new Message("queryParam", action));
    }

    private void checkRequestHeaderParameters(Values values, Action action) {
        new ParameterChecker(requestViolations).acceptWildcard().predefined(DefaultHeaders.REQUEST)
                .checkParameters(action.getHeaders(), values, new Message("headerParam", action));
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, Action action) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        final Map<String, List<? extends AbstractParam>> baseUriParams = getEffectiveBaseUriParams(action);
        paramChecker.checkListParameters(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", action));
        paramChecker.checkListParameters(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", action));
    }

    private Action findAction(Resource resource, String method) {
        Action action = resource.getAction(method);
        requestViolations.addAndThrowIf(action == null, "action.undefined", method, resource);
        return action;
    }

    private VariableMatcher getPathMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            requestViolations.addAndThrow("baseUri.unmatched", requestUri.getUri(), raml.getBaseUri());
        }
        return pathMatch;
    }

    private VariableMatcher getHostMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            requestViolations.addAndThrow("baseUri.unmatched", requestUri.getUri(), raml.getBaseUri());
        }
        return hostMatch;
    }

    private void checkProtocol(UriComponents requestUri, UriComponents ramlUri) {
        if (raml.getProtocols() != null && !raml.getProtocols().isEmpty()) {
            requestViolations.addIf(!raml.getProtocols().contains(protocolOf(requestUri.getScheme())), "protocol.undefined", requestUri.getScheme());
        } else {
            requestViolations.addIf(!ramlUri.getScheme().equalsIgnoreCase(requestUri.getScheme()), "protocol.undefined", requestUri.getScheme());
        }
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
        final Values values = new Values();
        final Resource resource = findResource(resourcePath, raml.getResources(), values);
        if (resource == null) {
            requestViolations.addAndThrow("resource.undefined", resourcePath);
        }
        checkUriParams(values, resource);
        return resource;
    }

    private void checkUriParams(Values values, Resource resource) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        for (Map.Entry<String, List<String>> entry : values) {
            final AbstractParam uriParam = findUriParam(entry.getKey(), resource);
            if (uriParam != null) {
                paramChecker.checkParameter(uriParam, entry.getValue().get(0), new Message("uriParam", entry.getKey(), resource));
            }
        }
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

    private Map<String, List<? extends AbstractParam>> getEffectiveBaseUriParams(Action action) {
        Map<String, List<? extends AbstractParam>> params = new HashMap<>();
        if (action.getBaseUriParameters() != null) {
            params.putAll(action.getBaseUriParameters());
        }
        addNotSetBaseUriParams(action.getResource(), params);
        return params;
    }

    private void addNotSetBaseUriParams(Resource resource, Map<String, List<? extends AbstractParam>> params) {
        if (resource.getBaseUriParameters() != null) {
            for (Map.Entry<String, List<UriParameter>> entry : resource.getBaseUriParameters().entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (resource.getParentResource() != null) {
            addNotSetBaseUriParams(resource.getParentResource(), params);
        } else if (raml.getBaseUriParameters() != null) {
            for (Map.Entry<String, UriParameter> entry : raml.getBaseUriParameters().entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                }
            }
        }
    }

    private Resource findResource(String resourcePath, Map<String, Resource> resources, Values values) {
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
                values.addValues(match.match.getVariables());
                return match.resource;
            }
            if (match.match.isMatch()) {
                values.addValues(match.match.getVariables());
                return findResource(match.match.getSuffix(), match.resource.getResources(), values);
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

    public void checkResponse(Action action, RamlResponse response) {
        Response res = findResponse(action, response.getStatus());
        checkResponseHeaderParameters(response.getHeaderValues(), action, res);
        checkBody(responseViolations, action, response, res.getBody(), new Message("response", response.getStatus()).toString());
    }

    private void checkBody(RamlViolations violations, Action action, RamlMessage message, Map<String, MimeType> bodies, String detail) {
        if (isNoOrEmptyBodies(bodies)) {
            violations.addIf(hasContent(message), "body.superfluous", action, detail);
        } else if (message.getContentType() == null) {
            violations.addAndThrowIf(hasContent(message) || !existSchemalessBody(bodies), "contentType.missing");
        } else {
            MediaType targetType = MediaType.valueOf(message.getContentType());
            MimeType mimeType = findMatchingMimeType(bodies, targetType);
            violations.addAndThrowIf(mimeType == null, "mediaType.undefined", message.getContentType(), action, detail);
            String schema = mimeType.getSchema();
            if (schema != null) {
                final SchemaValidator validator = findSchemaValidator(targetType);
                violations.addAndThrowIf(validator == null, "schemaValidator.missing", targetType, action, detail);
                final String content = message.getContent();
                String refSchema = raml.getConsolidatedSchemas().get(schema);
                schema = refSchema != null ? refSchema : schema;
                validator.validate(content, schema, violations, new Message("responseBody.mismatch", action, detail, mimeType, content));
            }
        }
    }

    private void checkResponseHeaderParameters(Values values, Action action, Response response) {
        new ParameterChecker(responseViolations).acceptWildcard().predefined(DefaultHeaders.RESPONSE)
                .checkParameters(response.getHeaders(), values, new Message("headerParam", action));
    }

    private Response findResponse(Action action, int status) {
        Response res = action.getResponses().get("" + status);
        responseViolations.addAndThrowIf(res == null, "responseCode.undefined", status, action);
        return res;
    }

    private SchemaValidator findSchemaValidator(MediaType mediaType) {
        for (SchemaValidator validator : schemaValidators) {
            if (validator.supports(mediaType)) {
                return validator;
            }
        }
        return null;
    }

    private boolean isNoOrEmptyBodies(Map<String, MimeType> bodies) {
        return bodies == null || bodies.isEmpty() || (bodies.size() == 1 && bodies.containsKey(null));
    }

    private boolean hasContent(RamlMessage message) {
        return message.getContent() != null && message.getContent().length() > 0;
    }

    private boolean existSchemalessBody(Map<String, MimeType> bodies) {
        for (MimeType mimeType : bodies.values()) {
            if (mimeType.getSchema() == null) {
                return true;
            }
        }
        return false;
    }

    private MimeType findMatchingMimeType(Map<String, MimeType> bodies, MediaType targetType) {
        try {
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
