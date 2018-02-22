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

import guru.nidi.ramltester.core.VariableMatcher.Match;
import guru.nidi.ramltester.model.*;
import guru.nidi.ramltester.model.internal.*;
import guru.nidi.ramltester.util.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.*;
import static guru.nidi.ramltester.core.UsageBuilder.*;

public class RamlChecker {
    private final CheckerConfig config;
    private final RamlApi api;
    private RamlViolations requestViolations;
    private RamlViolationsPerSecurity violationsPerSecurity;
    private Locator locator;
    private Usage usage;

    private static final class DefaultHeaders {
        private static final Set<String> REQUEST = new HashSet<>(Arrays.asList(
                "accept", "accept-charset", "accept-encoding", "accept-language",
                "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length",
                "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since",
                "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma",
                "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning"));

        private static final Set<String> RESPONSE = new HashSet<>(Arrays.asList(
                "access-control-allow-origin", "accept-ranges", "age", "allow",
                "cache-control", "connection", "content-encoding", "content-language", "content-length",
                "content-location", "content-md5", "content-disposition", "content-range", "content-type",
                "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma",
                "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status",
                "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via",
                "warning", "www-authenticate", "x-frame-options"));
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
            final RamlMethod method = findMethod(request);
            final SecurityExtractor security = new SecurityExtractor(api, method, requestViolations);
            violationsPerSecurity = new RamlViolationsPerSecurity(security);
            checkRequest(request, method, security);
            if (response != null) {
                checkResponse(request, response, method, security);
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

    public RamlMethod findMethod(RamlRequest request) {
        final UriComponents requestUri = UriComponents.fromHttpUrl(
                request.getRequestUrl(config.baseUri, config.includeServletPath));
        final UriComponents ramlUri = UriComponents.fromHttpUrl(
                api.baseUri() == null ? "http://server" : api.baseUri()); //dummy url as we only match paths
        final List<Match> hostMatches = api.baseUri() == null
                ? Collections.<Match>singletonList(null)
                : findHostMatches(requestUri, ramlUri);
        final List<Match> pathMatches = findPathMatches(requestUri, ramlUri);

        final RamlViolationsPerMatch violationsPerMethod = new RamlViolationsPerMatch();
        for (final Match hostMatch : hostMatches) {
            for (final Match pathMatch : pathMatches) {
                try {
                    final RamlViolations violations = violationsPerMethod.newViolations();
                    final RamlMethod method = findMethod(violations, pathMatch.suffix, request.getMethod());
                    violationsPerMethod.setMethod(method);
                    checkProtocol(method, requestUri, ramlUri);
                    checkBaseUriParameters(violations, hostMatch, pathMatch, method);
                    if (violations.isEmpty()) {
                        return method;
                    }
                } catch (RamlViolationException e) {
                    //try with next match
                }
            }
        }
        return violationsPerMethod.bestMethod(requestViolations);
    }

    private RamlMethod findMethod(RamlViolations violations, String path, String methodName) {
        final RamlResource resource = findResourceByPath(violations, path);
        final RamlMethod method = findMethod(resource, methodName);
        if (method == null) {
            violations.add("action.undefined", locator, methodName);
            throw new RamlViolationException();
        }
        resourceUsage(usage, resource).incUses(1);
        methodUsage(usage, method).incUses(1);
        locator.method(method);
        return method;
    }

    private RamlMethod findMethod(RamlResource resource, String methodName) {
        for (final RamlMethod method : resource.methods()) {
            if (method.method().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private RamlResource findResourceByPath(RamlViolations violations, String resourcePath) {
        final List<ResourceMatch> matches = ResourceMatch.find(resourcePath, api.resources());
        if (matches.isEmpty()) {
            violations.add("resource.undefined", resourcePath);
            throw new RamlViolationException();
        }
        if (matches.size() > 1) {
            for (int i = 0; i < matches.size(); i++) {
                for (int j = i + 1; j < matches.size(); j++) {
                    if (!matches.get(i).resource.resourcePath().equals(matches.get(j).resource.resourcePath())) {
                        violations.add("resource.ambiguous", resourcePath, matches.get(i).resource.relativeUri(),
                                matches.get(j).resource.relativeUri());
                        throw new RamlViolationException();
                    }
                }
            }
        }
        final RamlResource resource = matches.get(0).resource;
        locator.resource(resource);
        //checkUriParams(violations, values, resource);
        return resource;
    }

    public void checkRequest(RamlRequest request, RamlMethod method, SecurityExtractor security) {
        checkQueryParameters(request.getQueryValues(), method, security);
        checkRequestHeaderParameters(request.getHeaderValues(), method, security);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(requestViolations, request, method.body(), locator);
        if (typeMatch != null) {
            final RamlBody body = typeMatch.getMatchingBody();
            locator.requestBody(body);
            if (new FormDecoder(null, typeMatch.getTargetType()).supportsFormParameters()) {
                if (api.supportsFormParameters()) {
                    checkFormParameters(method, request.getFormValues(), body);
                } else {
                    final Values formValues = request.getFormValues();
                    bodyUsage(usage, method, body).addParameters(formValues.asMap().keySet());
                    new TypeChecker(requestViolations).check(body, formValues, new Message("messageBody", locator));
                }
            } else {
                checkSchema(requestViolations, request.getContent(), typeMatch);
            }
        }
    }

    private void checkFormParameters(RamlMethod method, Values values, RamlBody body) {
        if (body.type() != null) {
            requestViolations.add("schema.superfluous", locator);
        }
        @SuppressWarnings("unchecked")
        final List<RamlType> formParameters = body.formParameters();
        if (formParameters.isEmpty()) {
            requestViolations.add("formParameters.missing", locator);
        } else {
            checkFormParametersValues(method, body, values, formParameters);
        }
    }

    private void checkFormParametersValues(RamlMethod method, RamlBody body, Values values,
                                           List<RamlType> formParameters) {
        bodyUsage(usage, method, body).addParameters(
                new TypeChecker(requestViolations).check(formParameters, values, new Message("formParam", locator)));
    }

    private void checkQueryParameters(Values values, RamlMethod method, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final RamlSecScheme scheme : security.getSchemes()) {
            final Usage.Method a = methodUsage(usage, method);
            a.addQueryParameters(new TypeChecker(violationsPerSecurity.requestViolations(scheme))
                    .check(mergeLists(method.queryParameters(), security.queryParameters(scheme)), values,
                            new Message("queryParam", locator)));
        }
    }

    private void checkRequestHeaderParameters(Values values, RamlMethod method, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final RamlSecScheme scheme : security.getSchemes()) {
            final Usage.Method a = methodUsage(usage, method);
            a.addRequestHeaders(
                    new TypeChecker(violationsPerSecurity.requestViolations(scheme))
                            .acceptWildcard()
                            .ignoreX(config.ignoreXheaders)
                            .caseSensitive(false)
                            .predefined(DefaultHeaders.REQUEST)
                            .check(mergeLists(method.headers(), security.headers(scheme)), values,
                                    new Message("headerParam", locator)));
        }
    }

    private void checkBaseUriParameters(RamlViolations violations, Match hostMatch, Match pathMatch, RamlMethod method) {
        final TypeChecker checker = new TypeChecker(violations).acceptUndefined().ignoreRequired();
        final List<RamlType> baseUriParams = getEffectiveBaseUriParams(api.baseUriParameters(), method);
        if (hostMatch != null) {
            checker.check(baseUriParams, hostMatch.variables, new Message("baseUriParam", locator));
        }
        checker.check(baseUriParams, pathMatch.variables, new Message("baseUriParam", locator));
    }

    private List<Match> findPathMatches(UriComponents requestUri, UriComponents ramlUri) {
        final List<Match> matches = new VariableMatcher(ramlUri.getPath(), requestUri.getPath()).match();
        if (matches.isEmpty()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), api.baseUri());
            throw new RamlViolationException();
        }
        return matches;
    }

    private List<Match> findHostMatches(UriComponents requestUri, UriComponents ramlUri) {
        final List<Match> matches = new VariableMatcher(ramlUri.getHost(), requestUri.getHost()).match();
        for (final Iterator<Match> i = matches.iterator(); i.hasNext(); ) {
            final Match match = i.next();
            if (!match.isComplete()) {
                i.remove();
            }
        }
        if (matches.isEmpty()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), api.baseUri());
            throw new RamlViolationException();
        }
        return matches;
    }

    private void checkProtocol(RamlMethod method, UriComponents requestUri, UriComponents ramlUri) {
        final List<String> protocols = findProtocols(method, ramlUri.getScheme().toUpperCase());
        requestViolations.addIf(!protocols.contains(requestUri.getScheme().toUpperCase()), "protocol.undefined",
                locator, requestUri.getScheme());
    }

    private List<String> findProtocols(RamlMethod method, String fallback) {
        List<String> protocols = method.protocols();
        if (protocols == null || protocols.isEmpty()) {
            protocols = api.protocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(fallback);
        }
        return protocols;
    }

    private void checkUriParams(RamlViolations violations, Values values, RamlResource resource) {
        for (final Map.Entry<String, List<Object>> entry : values) {
            final RamlType uriParam = findUriParam(entry.getKey(), resource);
            final Message message = new Message("uriParam", locator, entry.getKey());
            if (uriParam != null) {
                new TypeChecker(violations).check(uriParam, entry.getValue().get(0), message);
            }
        }
    }

    public void checkResponse(RamlRequest request, RamlResponse response, RamlMethod method, SecurityExtractor security) {
        //TODO usage is multiplied by security schemes
        for (final RamlSecScheme scheme : security.getSchemes()) {
            final RamlViolations requestViolations = violationsPerSecurity.requestViolations(scheme);
            final RamlViolations responseViolations = violationsPerSecurity.responseViolations(scheme);
            final MediaTypeMatch typeMatch = doCheckReponse(responseViolations, response, method,
                    security.responses(scheme));
            if (typeMatch != null) {
                new ContentNegotiationChecker(requestViolations, responseViolations)
                        .check(request, response, method, typeMatch);
            }
        }
    }

    private MediaTypeMatch doCheckReponse(RamlViolations violations, RamlResponse response, RamlMethod method,
                                          List<RamlApiResponse> securityResponses) {
        final List<RamlApiResponse> responseMap = mergeLists(method.responses(), securityResponses);
        final RamlApiResponse res = responseByCode(responseMap, Integer.toString(response.getStatus()));
        if (res == null) {
            violations.add("responseCode.undefined", locator, response.getStatus());
            return null;
        }
        final String statusStr = Integer.toString(response.getStatus());
        methodUsage(usage, method).addResponseCode(statusStr);
        locator.responseCode(statusStr);
        checkResponseHeaderParameters(violations, response.getHeaderValues(), method, statusStr, res);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(violations, response, res.body(), locator);
        if (typeMatch != null) {
            locator.responseBody(typeMatch.getMatchingBody());
            checkSchema(violations, response.getContent(), typeMatch);
        }
        return typeMatch;
    }

    private void checkSchema(RamlViolations violations, byte[] body, MediaTypeMatch typeMatch) {
        final String typeDef = typeMatch.getMatchingBody().typeDefinition();
        final String type = typeMatch.getMatchingBody().type();
        if (typeDef == null && type == null) {
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
            validator.validate(new NamedReader(content, new Message("body").toString()), resolveSchema(type, typeDef),
                    violations, new Message("schema.body.mismatch", locator, content));
        } catch (UnsupportedEncodingException e) {
            violations.add("charset.invalid", charset);
        }
    }

    private void checkResponseHeaderParameters(RamlViolations violations, Values values, RamlMethod method,
                                               String responseCode, RamlApiResponse response) {
        final Usage.Response r = responseUsage(usage, method, responseCode);
        r.addResponseHeaders(
                new TypeChecker(violations)
                        .acceptWildcard()
                        .ignoreX(config.ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.RESPONSE)
                        .check(response.headers(), values, new Message("headerParam", locator)));
    }
}


