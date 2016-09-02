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

import guru.nidi.ramltester.model.*;
import guru.nidi.ramltester.util.FormDecoder;
import guru.nidi.ramltester.util.Message;
import guru.nidi.ramltester.util.UriComponents;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.*;
import static guru.nidi.ramltester.core.UsageBuilder.*;
import static guru.nidi.ramltester.model.UnifiedModel.responseByCode;

/**
 *
 */
public class RamlChecker {
    private final CheckerConfig config;
    private final UnifiedApi api;
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
        api = config.getRaml();
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
            final UnifiedMethod action = findAction(request);
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

    public UnifiedMethod findAction(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(request.getRequestUrl(config.baseUri, config.includeServletPath));
        if (api.baseUri() == null) {
            final UriComponents ramlUri = UriComponents.fromHttpUrl("http://server"); //dummy url as we only match paths
            final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);
            return findAction(pathMatch.getSuffix(), request.getMethod());
        }

        final UriComponents ramlUri = UriComponents.fromHttpUrl(api.baseUri());

        final VariableMatcher hostMatch = getHostMatch(requestUri, ramlUri);
        final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);

        final UnifiedMethod action = findAction(pathMatch.getSuffix(), request.getMethod());
        checkProtocol(action, requestUri, ramlUri);
        checkBaseUriParameters(hostMatch, pathMatch, action);

        return action;
    }

    private UnifiedMethod findAction(String path, String method) {
        final UnifiedResource resource = findResourceByPath(path);
        resourceUsage(usage, resource).incUses(1);
        final UnifiedMethod action = findAction(resource, method);
        if (action == null) {
            requestViolations.add("action.undefined", locator, method);
            throw new RamlViolationException();
        }
        actionUsage(usage, action).incUses(1);
        locator.action(action);
        return action;
    }

    private UnifiedMethod findAction(UnifiedResource resource, String method) {
        for (final UnifiedMethod action : resource.methods()) {
            if (action.method().equals(method.toLowerCase())) {
                return action;
            }
        }
        return null;
    }

    private UnifiedResource findResourceByPath(String resourcePath) {
        final Values values = new Values();
        final UnifiedResource resource = findResource(resourcePath, api.resources(), values);
        if (resource == null) {
            requestViolations.add("resource.undefined", resourcePath);
            throw new RamlViolationException();
        }
        locator.resource(resource);
        checkUriParams(values, resource);
        return resource;
    }

    public void checkRequest(RamlRequest request, UnifiedMethod action, SecurityExtractor security) {
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

    private void checkFormParameters(UnifiedMethod action, Values values, UnifiedBody mimeType) {
        if (mimeType.type() != null) {
            requestViolations.add("schema.superfluous", locator);
        }
        @SuppressWarnings("unchecked")
        final List<UnifiedType> formParameters = mimeType.formParameters();
        if (formParameters == null || formParameters.isEmpty()) {
            requestViolations.add("formParameters.missing", locator);
        } else {
            checkFormParametersValues(action, mimeType, values, formParameters);
        }
    }

    private void checkFormParametersValues(UnifiedMethod action, UnifiedBody mimeType, Values values, List<UnifiedType> formParameters) {
        final Usage.MimeType mt = mimeTypeUsage(usage, action, mimeType);
//            mt.addFormParameters(new ParameterChecker08(requestViolations)
//                    .checkListParameters(UnifiedModel.<Parameter>typeDelegates(formParameters), values, new Message("formParam", locator)));
        mt.addFormParameters(new TypeValidator(requestViolations).validate(formParameters, values, new Message("formParam", locator)));
    }

    private void checkQueryParameters(Values values, UnifiedMethod action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final UnifiedSecScheme scheme : security.getSchemes()) {
            final Usage.Action a = actionUsage(usage, action);
//                a.addQueryParameters(new ParameterChecker08(violationsPerSecurity.requestViolations(scheme))
//                        .checkParameters(UnifiedModel.<Parameter>typeDelegates(mergeLists(action.queryParameters(), security.queryParameters(scheme))), values, new Message("queryParam", locator)));
            a.addQueryParameters(new TypeValidator(violationsPerSecurity.requestViolations(scheme)).validate(mergeLists(action.queryParameters(), security.queryParameters(scheme)), values, new Message("queryParam", locator)));
        }
    }

    private void checkRequestHeaderParameters(Values values, UnifiedMethod action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final UnifiedSecScheme scheme : security.getSchemes()) {
            final Usage.Action a = actionUsage(usage, action);
            a.addRequestHeaders(
                    new TypeValidator(violationsPerSecurity.requestViolations(scheme))
                            .acceptWildcard()
                            .ignoreX(config.ignoreXheaders)
                            .caseSensitive(false)
                            .predefined(DefaultHeaders.REQUEST)
                            .validate(mergeLists(action.headers(), security.headers(scheme)), values, new Message("headerParam", locator)));
//            a.addRequestHeaders(
//                    new ParameterChecker08(violationsPerSecurity.requestViolations(scheme))
//                            .acceptWildcard()
//                            .ignoreX(config.ignoreXheaders)
//                            .caseSensitive(false)
//                            .predefined(DefaultHeaders.REQUEST)
//                            .checkParameters(UnifiedModel.<Parameter>typeDelegates(mergeLists(action.headers(), security.headers(scheme))), values, new Message("headerParam", locator)));
        }
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, UnifiedMethod action) {
//        final ParameterChecker08 paramChecker = new ParameterChecker08(requestViolations).acceptUndefined();
        final TypeValidator validator = new TypeValidator(requestViolations).acceptUndefined();
        final List<UnifiedType> baseUriParams = getEffectiveBaseUriParams(api.baseUriParameters(), action);
//            paramChecker.checkListParameters(UnifiedModel.<Parameter>typeDelegates(baseUriParams), hostMatch.getVariables(), new Message("baseUriParam", locator));
//            paramChecker.checkListParameters(UnifiedModel.<Parameter>typeDelegates(baseUriParams), pathMatch.getVariables(), new Message("baseUriParam", locator));
        validator.validate(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", locator));
        validator.validate(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", locator));
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

    private void checkProtocol(UnifiedMethod action, UriComponents requestUri, UriComponents ramlUri) {
        final List<String> protocols = findProtocols(action, ramlUri.getScheme().toUpperCase());
        requestViolations.addIf(!protocols.contains(requestUri.getScheme().toUpperCase()), "protocol.undefined", locator, requestUri.getScheme());
    }

    private List<String> findProtocols(UnifiedMethod action, String fallback) {
        List<String> protocols = action.protocols();
        if (protocols == null || protocols.isEmpty()) {
            protocols = api.protocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(fallback);
        }
        return protocols;
    }

    private void checkUriParams(Values values, UnifiedResource resource) {
//        final ParameterChecker08 paramChecker = new ParameterChecker08(requestViolations).acceptUndefined();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final UnifiedType uriParam = findUriParam(entry.getKey(), resource);
            final Message message = new Message("uriParam", locator, entry.getKey());
            if (uriParam != null) {
//                    paramChecker.checkParameter(uriParam, entry.getValue().get(0), message);
                uriParam.validate(entry.getValue().get(0), requestViolations, message);
            }
        }
    }

    public void checkResponse(RamlRequest request, RamlResponse response, UnifiedMethod action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final UnifiedSecScheme scheme : security.getSchemes()) {
            final RamlViolations requestViolations = violationsPerSecurity.requestViolations(scheme);
            final RamlViolations responseViolations = violationsPerSecurity.responseViolations(scheme);
            final MediaTypeMatch typeMatch = doCheckReponse(responseViolations, response, action, security.responses(scheme));
            if (typeMatch != null) {
                new ContentNegotiationChecker(requestViolations, responseViolations)
                        .check(request, response, action, typeMatch);
            }
        }
    }

    private MediaTypeMatch doCheckReponse(RamlViolations violations, RamlResponse response, UnifiedMethod action, List<UnifiedResponse> securityResponses) {
        final List<UnifiedResponse> responseMap = mergeLists(action.responses(), securityResponses);
        final UnifiedResponse res = responseByCode(responseMap, Integer.toString(response.getStatus()));
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
        final String schema = typeMatch.getMatchingMime().type();
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

    private void checkResponseHeaderParameters(RamlViolations violations, Values values, UnifiedMethod action, String responseCode, UnifiedResponse response) {
        final Usage.Response r = responseUsage(usage, action, responseCode);
        r.addResponseHeaders(
                new TypeValidator(violations)
                        .acceptWildcard()
                        .ignoreX(config.ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.RESPONSE)
                        .validate(response.headers(), values, new Message("headerParam", locator)));
//            r.addResponseHeaders(
//                    new ParameterChecker08(violations)
//                            .acceptWildcard()
//                            .ignoreX(config.ignoreXheaders)
//                            .caseSensitive(false)
//                            .predefined(DefaultHeaders.RESPONSE)
//                            .checkParameters(UnifiedModel.<Parameter>typeDelegates(response.headers()), values, new Message("headerParam", locator)));
    }
}


