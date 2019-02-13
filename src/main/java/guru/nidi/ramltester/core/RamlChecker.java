/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.*;
import guru.nidi.ramltester.util.*;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.*;
import static guru.nidi.ramltester.core.UsageBuilder.*;

/**
 *
 */
public class RamlChecker {
    private final CheckerConfig config;
    private RamlViolations requestViolations;
    private RamlViolationsPerSecurity violationsPerSecurity;
    private Locator locator;
    private Usage usage;

    private static final class DefaultHeaders {
        private static final Set<String>
                REQUEST = new HashSet<>(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language", "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length", "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma", "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning")),
                RESPONSE = new HashSet<>(Arrays.asList("referrer-policy", "access-control-allow-origin", "accept-ranges", "age", "allow", "cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-disposition", "content-range", "content-type", "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma", "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status", "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via", "warning", "www-authenticate", "x-frame-options"));
    }

    public RamlChecker(CheckerConfig config) {
        this.config = config;
    }

    public RamlReport check(RamlRequest request) {
        return check(request, null);
    }

    public RamlReport check(RamlRequest request, RamlResponse response) {
        final RamlReport report = new RamlReport(config.raml);
        usage = report.getUsage();
        requestViolations = report.getRequestViolations();
        final RamlViolations responseViolations = report.getResponseViolations();
        locator = new Locator();
        try {
            final Action action = findAction(request);
            final SecurityExtractor security = new SecurityExtractor(config.raml, action, requestViolations);
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

    public Action findAction(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(request.getRequestUrl(config.baseUri, config.includeServletPath));
        if (config.raml.getBaseUri() == null) {
            final UriComponents ramlUri = UriComponents.fromHttpUrl("http://server"); //dummy url as we only match paths
            final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);
            return findAction(pathMatch.getSuffix(), request.getMethod());
        }

        final UriComponents ramlUri = UriComponents.fromHttpUrl(config.raml.getBaseUri());

        final VariableMatcher hostMatch = getHostMatch(requestUri, ramlUri);
        final VariableMatcher pathMatch = getPathMatch(requestUri, ramlUri);

        final Action action = findAction(pathMatch.getSuffix(), request.getMethod());
        checkProtocol(action, requestUri, ramlUri);
        checkBaseUriParameters(hostMatch, pathMatch, action);

        return action;
    }

    private Action findAction(String path, String method) {
        final Resource resource = findResourceByPath(path);
        resourceUsage(usage, resource).incUses(1);
        final Action action = resource.getAction(method);
        if (action == null) {
            requestViolations.add("action.undefined", locator, method);
            throw new RamlViolationException();
        }
        actionUsage(usage, action).incUses(1);
        locator.action(action);
        return action;
    }

    private Resource findResourceByPath(String resourcePath) {
        final Values values = new Values();
        final List<ResourceMatch> matches = findResource(resourcePath, config.raml.getResources(), values);
        if (matches.isEmpty()) {
            requestViolations.add("resource.undefined", resourcePath);
            throw new RamlViolationException();
        }
        if (matches.size() > 1 && matches.get(0).compareTo(matches.get(1)) == 0) {
            requestViolations.add("resource.ambiguous", resourcePath, matches.get(0).resource.getUri(), matches.get(1).resource.getUri());
            throw new RamlViolationException();
        }
        final Resource resource = matches.get(0).resource;
        locator.resource(resource);
        checkUriParams(values, resource);
        return resource;
    }

    public void checkRequest(RamlRequest request, Action action, SecurityExtractor security) {
        checkQueryParameters(request.getQueryValues(), action, security);
        checkRequestHeaderParameters(request.getHeaderValues(), action, security);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(requestViolations, request, action.getBody(), locator);
        if (typeMatch != null) {
            locator.requestMime(typeMatch.getMatchingMime());
            if (FormDecoder.supportsFormParameters(typeMatch.getTargetType())) {
                checkFormParameters(action, request.getFormValues(), typeMatch.getMatchingMime());
            } else {
                checkSchema(requestViolations, request.getContent(), typeMatch);
            }
        }
    }

    private void checkFormParameters(Action action, Values values, MimeType mimeType) {
        if (mimeType.getSchema() != null) {
            requestViolations.add("schema.superfluous", locator);
        }
        @SuppressWarnings("unchecked")
        final Map<String, List<? extends AbstractParam>> formParameters = (Map) mimeType.getFormParameters();
        if (formParameters == null || formParameters.isEmpty()) {
            requestViolations.add("formParameters.missing", locator);
        } else {
            checkFormParametersValues(action, mimeType, values, formParameters);
        }
    }

    private void checkFormParametersValues(Action action, MimeType mimeType, Values values, Map<String, List<? extends AbstractParam>> formParameters) {
        mimeTypeUsage(usage, action, mimeType).addFormParameters(
                new ParameterChecker(requestViolations)
                        .checkListParameters(formParameters, values, new Message("formParam", locator))
        );
    }

    private void checkQueryParameters(Values values, Action action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final SecurityScheme scheme : security.getSchemes()) {
            actionUsage(usage, action).addQueryParameters(
                    new ParameterChecker(violationsPerSecurity.requestViolations(scheme))
                            .checkParameters(mergeMaps(action.getQueryParameters(), security.queryParameters(scheme)), values, new Message("queryParam", locator))
            );
        }
    }

    private void checkRequestHeaderParameters(Values values, Action action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final SecurityScheme scheme : security.getSchemes()) {
            actionUsage(usage, action).addRequestHeaders(
                    new ParameterChecker(violationsPerSecurity.requestViolations(scheme))
                            .acceptWildcard()
                            .ignoreX(config.ignoreXheaders)
                            .caseSensitive(false)
                            .predefined(DefaultHeaders.REQUEST)
                            .checkParameters(mergeMaps(action.getHeaders(), security.headers(scheme)), values, new Message("headerParam", locator))
            );
        }
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, Action action) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        final Map<String, List<? extends AbstractParam>> baseUriParams = getEffectiveBaseUriParams(config.raml.getBaseUriParameters(), action);
        paramChecker.checkListParameters(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", locator));
        paramChecker.checkListParameters(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", locator));
    }

    private VariableMatcher getPathMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), config.raml.getBaseUri());
            throw new RamlViolationException();
        }
        return pathMatch;
    }

    private VariableMatcher getHostMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), config.raml.getBaseUri());
            throw new RamlViolationException();
        }
        return hostMatch;
    }

    private void checkProtocol(Action action, UriComponents requestUri, UriComponents ramlUri) {
        final List<Protocol> protocols = findProtocols(action, ramlUri.getScheme());
        requestViolations.addIf(!protocols.contains(protocolOf(requestUri.getScheme())), "protocol.undefined", locator, requestUri.getScheme());
    }

    private List<Protocol> findProtocols(Action action, String fallback) {
        List<Protocol> protocols = action.getProtocols();
        if (protocols == null || protocols.isEmpty()) {
            protocols = config.raml.getProtocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(Protocol.valueOf(fallback.toUpperCase(Locale.ENGLISH)));
        }
        return protocols;
    }

    private void checkUriParams(Values values, Resource resource) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final AbstractParam uriParam = findUriParam(entry.getKey(), resource);
            final Message message = new Message("uriParam", locator, entry.getKey());
            if (uriParam != null) {
                paramChecker.checkParameter(uriParam, entry.getValue().get(0), message);
            }
        }
    }

    public void checkResponse(RamlRequest request, RamlResponse response, Action action, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final SecurityScheme scheme : security.getSchemes()) {
            final RamlViolations requestViolations = violationsPerSecurity.requestViolations(scheme);
            final RamlViolations responseViolations = violationsPerSecurity.responseViolations(scheme);
            final MediaTypeMatch typeMatch = doCheckReponse(responseViolations, response, action, security.responses(scheme));
            if (typeMatch != null) {
                new ContentNegotiationChecker(requestViolations, responseViolations)
                        .check(request, response, action, typeMatch);
            }
        }
    }

    private MediaTypeMatch doCheckReponse(RamlViolations violations, RamlResponse response, Action action, Map<String, Response> securityResponses) {
        final Map<String, Response> responseMap = mergeMaps(action.getResponses(), securityResponses);
        final Response res = responseMap.get(Integer.toString(response.getStatus()));
        if (res == null) {
            violations.add("responseCode.undefined", locator, response.getStatus());
            return null;
        }
        final String statusStr = Integer.toString(response.getStatus());
        actionUsage(usage, action).addResponseCode(statusStr);
        locator.responseCode(statusStr);
        checkResponseHeaderParameters(violations, response.getHeaderValues(), action, statusStr, res);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(violations, response, res.getBody(), locator);
        if (typeMatch != null) {
            locator.responseMime(typeMatch.getMatchingMime());
            checkSchema(violations, response.getContent(), typeMatch);
        }
        return typeMatch;
    }

    private void checkSchema(RamlViolations violations, byte[] body, MediaTypeMatch typeMatch) {
        final String schema = typeMatch.getMatchingMime().getSchema();
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
            validator.validate(new NamedReader(content, new Message("body").toString()), resolveSchema(config.raml, schema), violations, new Message("schema.body.mismatch", locator, content));
        } catch (UnsupportedEncodingException e) {
            violations.add("charset.invalid", charset);
        }
    }

    private void checkResponseHeaderParameters(RamlViolations violations, Values values, Action action, String responseCode, Response response) {
        responseUsage(usage, action, responseCode).addResponseHeaders(
                new ParameterChecker(violations)
                        .acceptWildcard()
                        .ignoreX(config.ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.RESPONSE)
                        .checkParameters(response.getHeaders(), values, new Message("headerParam", locator))
        );
    }
}
