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

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FormDecoder;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.UriComponents;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.core.UsageBuilder.*;

/**
 *
 */
public class RamlChecker {
    private static final class DefaultHeaders {
        private static final Set<String>
                REQUEST = new HashSet<>(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language", "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length", "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma", "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning")),
                RESPONSE = new HashSet<>(Arrays.asList("access-control-allow-origin", "accept-ranges", "age", "allow", "cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-disposition", "content-range", "content-type", "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma", "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status", "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via", "warning", "www-authenticate", "x-frame-options"));
    }

    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final String baseUri;
    private final boolean ignoreXheaders;
    private RamlViolations requestViolations, responseViolations;
    private Usage usage;

    public RamlChecker(Raml raml, List<SchemaValidator> schemaValidators, String baseUri, boolean ignoreXheaders) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.baseUri = baseUri;
        this.ignoreXheaders = ignoreXheaders;
    }

    public RamlReport check(RamlRequest request, RamlResponse response) {
        final RamlReport report = new RamlReport(raml);
        usage = report.getUsage();
        requestViolations = report.getRequestViolations();
        responseViolations = report.getResponseViolations();
        try {
            checkSecurity(raml.getSecuritySchemes());
            final Action action = findAction(request);
            final SecurityExtractor security = new SecurityExtractor(raml, action, requestViolations);
            checkRequest(request, action, security);
            if (response != null) {
                checkResponse(response, action, security);
            }
        } catch (RamlViolationException e) {
            //ignore, results are in report
        }
        return report;
    }

    private void checkSecurity(List<Map<String, SecurityScheme>> schemes) {
        for (final Map<String, SecurityScheme> schemeMap : schemes) {
            for (final SecurityScheme scheme : schemeMap.values()) {
                final SecuritySchemeType type = SecuritySchemeType.byName(scheme.getType());
                if (type != null) {
                    type.check(scheme, requestViolations);
                }
            }
        }
    }

    public RamlReport check(RamlRequest request) {
        return check(request, null);
    }

    public Action findAction(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(request.getRequestUrl(baseUri));
        if (raml.getBaseUri() == null) {
            final UriComponents ramlUri = UriComponents.fromHttpUrl("http://server"); //dummy url as we only match paths
            final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);
            return findAction(pathMatch.getSuffix(), request.getMethod());
        }

        final UriComponents ramlUri = UriComponents.fromHttpUrl(raml.getBaseUri());

        final VariableMatcher hostMatch = getHostMatch(requestUri, ramlUri);
        final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);

        final Action action = findAction(pathMatch.getSuffix(), request.getMethod());
        checkProtocol(action, requestUri, ramlUri);
        checkBaseUriParameters(hostMatch, pathMatch, action);

        return action;
    }

    private Action findAction(String path, String method) {
        final Resource resource = findResource(path);
        resourceUsage(usage, resource).incUses(1);
        final Action action = resource.getAction(method);
        requestViolations.addAndThrowIf(action == null, "action.undefined", method, resource);
        actionUsage(usage, action).incUses(1);
        return action;
    }

    public void checkRequest(RamlRequest request, Action action, SecurityExtractor security) {
        checkQueryParameters(request.getQueryValues(), action, security);
        checkRequestHeaderParameters(request.getHeaderValues(), action, security);

        final CheckerType type = CheckerType.find(requestViolations, action, request, action.getBody(), "");
        if (type != null) {
            if (FormDecoder.supportsFormParameters(type.getMedia())) {
                checkFormParameters(action, request.getFormValues(), type.getMime());
            } else {
                checkSchema(requestViolations, action, request.getContent(), type, "");
            }
        }
    }

    private void checkFormParameters(Action action, Values values, MimeType mimeType) {
        if (mimeType.getSchema() != null) {
            requestViolations.add("schema.superfluous", action, mimeType);
        }
        @SuppressWarnings("unchecked")
        final Map<String, List<? extends AbstractParam>> formParameters = (Map) mimeType.getFormParameters();
        if (formParameters == null) {
            requestViolations.add("formParameters.missing", action, mimeType);
        } else {
            checkFormParametersValues(action, mimeType, values, formParameters);
        }
    }

    private void checkFormParametersValues(Action action, MimeType mimeType, Values values, Map<String, List<? extends AbstractParam>> formParameters) {
        mimeTypeUsage(usage, action, mimeType).addFormParameters(
                new ParameterChecker(requestViolations)
                        .checkListParameters(formParameters, values, new Message("formParam", action))
        );
    }

    private void checkQueryParameters(Values values, Action action, SecurityExtractor security) {
        actionUsage(usage, action).addQueryParameters(
                new ParameterChecker(requestViolations)
                        .checkParameters(action.getQueryParameters(), security.queryParameters(), values, new Message("queryParam", action))
        );
    }

    private void checkRequestHeaderParameters(Values values, Action action, SecurityExtractor security) {
        actionUsage(usage, action).addRequestHeaders(
                new ParameterChecker(requestViolations)
                        .acceptWildcard()
                        .ignoreX(ignoreXheaders)
                        .predefined(DefaultHeaders.REQUEST)
                        .checkParameters(action.getHeaders(), security.headers(), values, new Message("headerParam", action))
        );
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, Action action) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        final Map<String, List<? extends AbstractParam>> baseUriParams = getEffectiveBaseUriParams(action);
        paramChecker.checkListParameters(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", action));
        paramChecker.checkListParameters(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", action));
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

    private void checkProtocol(Action action, UriComponents requestUri, UriComponents ramlUri) {
        final List<Protocol> protocols = findProtocols(action, ramlUri.getScheme());
        requestViolations.addIf(!protocols.contains(CheckerHelper.protocolOf(requestUri.getScheme())), "protocol.undefined", requestUri.getScheme(), action);
    }

    private List<Protocol> findProtocols(Action action, String fallback) {
        List<Protocol> protocols = action.getProtocols();
        if (protocols == null || protocols.isEmpty()) {
            protocols = raml.getProtocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(Protocol.valueOf(fallback.toUpperCase()));
        }
        return protocols;
    }

    private Resource findResource(String resourcePath) {
        final Values values = new Values();
        final Resource resource = CheckerHelper.findResource(resourcePath, raml.getResources(), values);
        if (resource == null) {
            requestViolations.addAndThrow("resource.undefined", resourcePath);
        }
        checkUriParams(values, resource);
        return resource;
    }

    private void checkUriParams(Values values, Resource resource) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final AbstractParam uriParam = CheckerHelper.findUriParam(entry.getKey(), resource);
            final Message message = new Message("uriParam", entry.getKey(), resource);
            if (uriParam != null) {
                paramChecker.checkParameter(uriParam, entry.getValue().get(0), message);
            }
        }
    }

    private Map<String, List<? extends AbstractParam>> getEffectiveBaseUriParams(Action action) {
        final Map<String, List<? extends AbstractParam>> params = new HashMap<>();
        if (action.getBaseUriParameters() != null) {
            params.putAll(action.getBaseUriParameters());
        }
        addNotSetBaseUriParams(action.getResource(), params);
        return params;
    }

    private void addNotSetBaseUriParams(Resource resource, Map<String, List<? extends AbstractParam>> params) {
        if (resource.getBaseUriParameters() != null) {
            for (final Map.Entry<String, List<UriParameter>> entry : resource.getBaseUriParameters().entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (resource.getParentResource() != null) {
            addNotSetBaseUriParams(resource.getParentResource(), params);
        } else if (raml.getBaseUriParameters() != null) {
            for (final Map.Entry<String, UriParameter> entry : raml.getBaseUriParameters().entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                }
            }
        }
    }

    public void checkResponse(RamlResponse response, Action action, SecurityExtractor security) {
        final Response res = findResponse(action, response.getStatus(), security);
        actionUsage(usage, action).addResponseCode("" + response.getStatus());
        checkResponseHeaderParameters(response.getHeaderValues(), action, "" + response.getStatus(), res);

        final String detail = new Message("response", response.getStatus()).toString();
        final CheckerType type = CheckerType.find(responseViolations, action, response, res.getBody(), detail);
        checkSchema(responseViolations, action, response.getContent(), type, detail);
    }

    private void checkSchema(RamlViolations violations, Action action, byte[] body, CheckerType type, String detail) {
        if (type == null) {
            return;
        }
        final String schema = type.getMime().getSchema();
        if (schema == null) {
            return;
        }
        final SchemaValidator validator = findSchemaValidator(type.getMedia());
        if (validator == null) {
            violations.add("schemaValidator.missing", type.getMedia(), action, detail);
            return;
        }
        if (body == null || body.length == 0) {
            violations.add("body.empty", type.getMedia(), action, detail);
            return;
        }

        final String charset = type.getCharset();
        try {
            final String content = new String(body, charset);
            final String refSchema = raml.getConsolidatedSchemas().get(schema);
            final String schemaToUse = refSchema == null ? schema : refSchema;
            validator.validate(content, schemaToUse, violations, new Message("schema.mismatch", action, detail, type.getMime(), content));
        } catch (UnsupportedEncodingException e) {
            violations.add("charset.invalid", charset);
        }
    }

    private void checkResponseHeaderParameters(Values values, Action action, String responseCode, Response response) {
        responseUsage(usage, action, responseCode).addResponseHeaders(
                new ParameterChecker(responseViolations)
                        .acceptWildcard()
                        .ignoreX(ignoreXheaders)
                        .predefined(DefaultHeaders.RESPONSE)
                        .checkParameters(response.getHeaders(), values, new Message("headerParam", action))
        );
    }

    private Response findResponse(Action action, int status, SecurityExtractor security) {
        Response res = action.getResponses().get("" + status);
        if (res == null) {
            final Iterator<Map<String, Response>> iter = security.responses().iterator();
            //there could be more that 1 matching response, problem?
            while (iter.hasNext()) {
                final Map<String, Response> resMap = iter.next();
                res = resMap.get("" + status);
                if (res == null) {
                    iter.remove();
                }
            }
        }
        responseViolations.addAndThrowIf(res == null, "responseCode.undefined", status, action);
        return res;
    }

    private SchemaValidator findSchemaValidator(MediaType mediaType) {
        for (final SchemaValidator validator : schemaValidators) {
            if (validator.supports(mediaType)) {
                return validator;
            }
        }
        return null;
    }

}


