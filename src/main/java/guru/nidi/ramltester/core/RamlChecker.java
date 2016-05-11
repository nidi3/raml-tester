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
import guru.nidi.ramltester.util.Message;
import guru.nidi.ramltester.util.UriComponents;
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.bodies.BodyLike;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.resources.Resource;
import org.raml.v2.api.model.v08.security.AbstractSecurityScheme;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.*;
import static guru.nidi.ramltester.core.UsageBuilder.*;

/**
 *
 */
public class RamlChecker {
    private final CheckerConfig config;
    private final Api api;
    private RamlViolations requestViolations;
    private RamlViolationsPerSecurity violationsPerSecurity;
    private Locator locator;
    private Usage usage;

    private static final class DefaultHeaders {
        private static final Set<String>
                REQUEST = new HashSet<>(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language", "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length", "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma", "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning")),
                RESPONSE = new HashSet<>(Arrays.asList("access-control-allow-origin", "accept-ranges", "age", "allow", "cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-disposition", "content-range", "content-type", "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma", "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status", "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via", "warning", "www-authenticate", "x-frame-options"));
    }

    public RamlChecker(CheckerConfig config) {
        this.config = config;
        api = config.raml.getApiV08();
    }

    public RamlReport check(RamlRequest request) {
        return check(request, null);
    }

    public RamlReport check(RamlRequest request, RamlResponse response) {
        final RamlReport report = new RamlReport(api);
        usage = report.getUsage();
        requestViolations = report.getRequestViolations();
        final RamlViolations responseViolations = report.getResponseViolations();
        locator = new Locator();
        try {
            final Method action = findAction(request);
            final SecurityExtractor security = new SecurityExtractor(api, action, requestViolations);
            security.check(requestViolations);
            violationsPerSecurity = new RamlViolationsPerSecurity(security);
            checkRequest(request, action, security);
            if (response != null) {
                checkResponse(request, response, action, security);
            }
            violationsPerSecurity.addLeastViolations(requestViolations, responseViolations);
        } catch (RamlViolationException e) {
            //ignore, results are in report
        }

        if (config.failFast && !report.isEmpty()) {
            throw new RamlViolationException(report);
        }

        return report;
    }

    public Method findAction(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(request.getRequestUrl(config.baseUri, config.includeServletPath));
        if (api.baseUri() == null) {
            final UriComponents ramlUri = UriComponents.fromHttpUrl("http://server"); //dummy url as we only match paths
            final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);
            return findAction(pathMatch.getSuffix(), request.getMethod());
        }

        final UriComponents ramlUri = UriComponents.fromHttpUrl(api.baseUri().value());

        final VariableMatcher hostMatch = getHostMatch(requestUri, ramlUri);
        final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);

        final Method action = findAction(pathMatch.getSuffix(), request.getMethod());
        checkProtocol(action, requestUri, ramlUri);
        checkBaseUriParameters(hostMatch, pathMatch, action);

        return action;
    }

    private Method findAction(String path, String method) {
        final Resource resource = findResourceByPath(path);
        resourceUsage(usage, resource).incUses(1);
        final Method action = findAction(resource, method);
        if (action == null) {
            requestViolations.add("action.undefined", locator, method);
            throw new RamlViolationException();
        }
        actionUsage(usage, action).incUses(1);
        locator.action(action);
        return action;
    }

    private Method findAction(Resource resource, String method) {
        for (final Method action : resource.methods()) {
            if (action.method().equals(method)) {
                return action;
            }
        }
        return null;
    }

    private Resource findResourceByPath(String resourcePath) {
        final Values values = new Values();
        final Resource resource = findResource(resourcePath, api.resources(), values);
        if (resource == null) {
            requestViolations.add("resource.undefined", resourcePath);
            throw new RamlViolationException();
        }
        locator.resource(resource);
        checkUriParams(values, resource);
        return resource;
    }

    public void checkRequest(RamlRequest request, Method action, SecurityExtractor security) {
        checkQueryParameters(request.getQueryValues(), action, security);
        checkRequestHeaderParameters(request.getHeaderValues(), action, security);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(requestViolations, request, action.body(), locator);
        if (typeMatch != null) {
            locator.requestMime(typeMatch.getMatchingMime());
            if (FormDecoder.supportsFormParameters(typeMatch.getTargetType())) {
                checkFormParameters(action, request.getFormValues(), typeMatch.getMatchingMime());
            } else {
                checkSchema(requestViolations, request.getContent(), typeMatch);
            }
        }
    }

    private void checkFormParameters(Method action, Values values, BodyLike mimeType) {
        if (mimeType.schema() != null) {
            requestViolations.add("schema.superfluous", locator);
        }
        @SuppressWarnings("unchecked")
        final List<Parameter> formParameters = mimeType.formParameters();
        if (formParameters == null) {
            requestViolations.add("formParameters.missing", locator);
        } else {
            checkFormParametersValues(action, mimeType, values, formParameters);
        }
    }

    private void checkFormParametersValues(Method action, BodyLike mimeType, Values values, List<Parameter> formParameters) {
        mimeTypeUsage(usage, action, mimeType).addFormParameters(
                new ParameterChecker(requestViolations)
                        .checkListParameters(formParameters, values, new Message("formParam", locator))
        );
    }

    private void checkQueryParameters(Values values, Method action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final AbstractSecurityScheme scheme : security.getSchemes()) {
            actionUsage(usage, action).addQueryParameters(
                    new ParameterChecker(violationsPerSecurity.requestViolations(scheme))
                            .checkParameters(mergeLists(action.queryParameters(), security.queryParameters(scheme)), values, new Message("queryParam", locator))
            );
        }
    }

    private void checkRequestHeaderParameters(Values values, Method action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final AbstractSecurityScheme scheme : security.getSchemes()) {
            actionUsage(usage, action).addRequestHeaders(
                    new ParameterChecker(violationsPerSecurity.requestViolations(scheme))
                            .acceptWildcard()
                            .ignoreX(config.ignoreXheaders)
                            .caseSensitive(false)
                            .predefined(DefaultHeaders.REQUEST)
                            .checkParameters(mergeLists(action.headers(), security.headers(scheme)), values, new Message("headerParam", locator))
            );
        }
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, Method action) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        final List<Parameter> baseUriParams = getEffectiveBaseUriParams(api.baseUriParameters(), action);
        paramChecker.checkListParameters(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", locator));
        paramChecker.checkListParameters(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", locator));
    }

    private VariableMatcher getPathMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), api.baseUri());
            throw new RamlViolationException();
        }
        return pathMatch;
    }

    private VariableMatcher getHostMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), api.baseUri());
            throw new RamlViolationException();
        }
        return hostMatch;
    }

    private void checkProtocol(Method action, UriComponents requestUri, UriComponents ramlUri) {
        final List<String> protocols = findProtocols(action, ramlUri.getScheme());
        requestViolations.addIf(!protocols.contains(requestUri.getScheme()), "protocol.undefined", locator, requestUri.getScheme());
    }

    private List<String> findProtocols(Method action, String fallback) {
        List<String> protocols = action.protocols();
        if (protocols == null || protocols.isEmpty()) {
            protocols = api.protocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(fallback);
        }
        return protocols;
    }

    private void checkUriParams(Values values, Resource resource) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final Parameter uriParam = findUriParam(entry.getKey(), resource);
            final Message message = new Message("uriParam", locator, entry.getKey());
            if (uriParam != null) {
                paramChecker.checkParameter(uriParam, entry.getValue().get(0), message);
            }
        }
    }

    public void checkResponse(RamlRequest request, RamlResponse response, Method action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final AbstractSecurityScheme scheme : security.getSchemes()) {
            final RamlViolations requestViolations = violationsPerSecurity.requestViolations(scheme);
            final RamlViolations responseViolations = violationsPerSecurity.responseViolations(scheme);
            final MediaTypeMatch typeMatch = doCheckReponse(responseViolations, response, action, security.responses(scheme));
            if (typeMatch != null) {
                new ContentNegotiationChecker(requestViolations, responseViolations)
                        .check(request, response, action, typeMatch);
            }
        }
    }

    private MediaTypeMatch doCheckReponse(RamlViolations violations, RamlResponse response, Method action, List<Response> securityResponses) {
        final List<Response> responseMap = mergeLists(action.responses(), securityResponses);
        final Response res = responseByCode(responseMap, Integer.toString(response.getStatus()));
        if (res == null) {
            violations.add("responseCode.undefined", locator, response.getStatus());
            return null;
        }
        final String statusStr = Integer.toString(response.getStatus());
        actionUsage(usage, action).addResponseCode(statusStr);
        locator.responseCode(statusStr);
        checkResponseHeaderParameters(violations, response.getHeaderValues(), action, statusStr, res);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(violations, response, res.body(), locator);
        if (typeMatch != null) {
            locator.responseMime(typeMatch.getMatchingMime());
            checkSchema(violations, response.getContent(), typeMatch);
        }
        return typeMatch;
    }

    private void checkSchema(RamlViolations violations, byte[] body, MediaTypeMatch typeMatch) {
        final String schema = typeMatch.getMatchingMime().schemaContent();
        if (schema == null) {
            return;
        }
        final SchemaValidator validator = findSchemaValidator(config.schemaValidators, typeMatch.getTargetType());
        if (validator == null) {
            violations.add("schemaValidator.missing", locator, typeMatch.getTargetType());
            return;
        }
        if (body == null || body.length == 0) {
            violations.add("body.empty", locator, typeMatch.getTargetType());
            return;
        }

        final String charset = typeMatch.getTargetCharset();
        try {
            final String content = new String(body, charset);
            validator.validate(new NamedReader(content, new Message("body").toString()), resolveSchema(api, schema), violations, new Message("schema.body.mismatch", locator, content));
        } catch (UnsupportedEncodingException e) {
            violations.add("charset.invalid", charset);
        }
    }

    private void checkResponseHeaderParameters(RamlViolations violations, Values values, Method action, String responseCode, Response response) {
        responseUsage(usage, action, responseCode).addResponseHeaders(
                new ParameterChecker(violations)
                        .acceptWildcard()
                        .ignoreX(config.ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.RESPONSE)
                        .checkParameters(response.headers(), values, new Message("headerParam", locator))
        );
    }
}


