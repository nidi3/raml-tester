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
    private static final class DefaultHeaders {
        private static final Set<String>
                REQUEST = new HashSet<>(Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language", "accept-datetime", "authorization", "cache-control", "connection", "cookie", "content-length", "content-md5", "content-type", "date", "dnt", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "max-forwards", "origin", "pragma", "proxy-authorization", "range", "referer", "te", "user-agent", "upgrade", "via", "warning")),
                RESPONSE = new HashSet<>(Arrays.asList("access-control-allow-origin", "accept-ranges", "age", "allow", "cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-disposition", "content-range", "content-type", "date", "etag", "expires", "last-modified", "link", "location", "p3p", "pragma", "proxy-authenticate", "refresh", "retry-after", "server", "set-cookie", "status", "strict-transport-security", "trailer", "transfer-encoding", "upgrade", "vary", "via", "warning", "www-authenticate", "x-frame-options"));
    }

    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private final String baseUri;
    private final boolean ignoreXheaders;
    private final boolean failFast;

    private RamlViolations requestViolations, responseViolations;
    private Locator locator;
    private Usage usage;

    public RamlChecker(Raml raml, List<SchemaValidator> schemaValidators, String baseUri, boolean ignoreXheaders, boolean failFast) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.baseUri = baseUri;
        this.ignoreXheaders = ignoreXheaders;
        this.failFast = failFast;
    }

    public RamlReport check(RamlRequest request) {
        return check(request, null);
    }

    public RamlReport check(RamlRequest request, RamlResponse response) {
        final RamlReport report = new RamlReport(raml);
        usage = report.getUsage();
        requestViolations = report.getRequestViolations();
        responseViolations = report.getResponseViolations();
        locator = new Locator();
        try {
            final Action action = findAction(request);
            final SecurityExtractor security = new SecurityExtractor(raml, action, requestViolations);
            security.check(requestViolations);
            checkRequest(request, action, security);
            if (response != null) {
                final MediaTypeMatch typeMatch = checkResponse(response, action, security);
                new ContentNegotiationChecker(requestViolations, responseViolations)
                        .check(request, response, action, typeMatch);
            }
        } catch (RamlViolationException e) {
            //ignore, results are in report
        }

        if (failFast && !report.isEmpty()) {
            throw new RamlViolationException(report);
        }

        return report;
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
        final Resource resource = findResource(resourcePath, raml.getResources(), values);
        if (resource == null) {
            requestViolations.add("resource.undefined", resourcePath);
            throw new RamlViolationException();
        }
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
        if (formParameters == null) {
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
        actionUsage(usage, action).addQueryParameters(
                new ParameterChecker(requestViolations)
                        .checkParameters(action.getQueryParameters(), security.queryParameters(), values, new Message("queryParam", locator))
        );
    }

    private void checkRequestHeaderParameters(Values values, Action action, SecurityExtractor security) {
        actionUsage(usage, action).addRequestHeaders(
                new ParameterChecker(requestViolations)
                        .acceptWildcard()
                        .ignoreX(ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.REQUEST)
                        .checkParameters(action.getHeaders(), security.headers(), values, new Message("headerParam", locator))
        );
    }

    private void checkBaseUriParameters(VariableMatcher hostMatch, VariableMatcher pathMatch, Action action) {
        final ParameterChecker paramChecker = new ParameterChecker(requestViolations).acceptUndefined();
        final Map<String, List<? extends AbstractParam>> baseUriParams = getEffectiveBaseUriParams(raml.getBaseUriParameters(), action);
        paramChecker.checkListParameters(baseUriParams, hostMatch.getVariables(), new Message("baseUriParam", locator));
        paramChecker.checkListParameters(baseUriParams, pathMatch.getVariables(), new Message("baseUriParam", locator));
    }

    private VariableMatcher getPathMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher pathMatch = VariableMatcher.match(ramlUri.getPath(), requestUri.getPath());
        if (!pathMatch.isMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), raml.getBaseUri());
            throw new RamlViolationException();
        }
        return pathMatch;
    }

    private VariableMatcher getHostMatch(UriComponents requestUri, UriComponents ramlUri) {
        final VariableMatcher hostMatch = VariableMatcher.match(ramlUri.getHost(), requestUri.getHost());
        if (!hostMatch.isCompleteMatch()) {
            requestViolations.add("baseUri.unmatched", requestUri.getUri(), raml.getBaseUri());
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
            protocols = raml.getProtocols();
        }
        if (protocols == null || protocols.isEmpty()) {
            protocols = Collections.singletonList(Protocol.valueOf(fallback.toUpperCase()));
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

    public MediaTypeMatch checkResponse(RamlResponse response, Action action, SecurityExtractor security) {
        final Response res = findResponse(action, response.getStatus(), security);
        if (res == null) {
            responseViolations.add("responseCode.undefined", locator, response.getStatus());
            throw new RamlViolationException();
        }
        actionUsage(usage, action).addResponseCode("" + response.getStatus());
        locator.responseCode("" + response.getStatus());
        checkResponseHeaderParameters(response.getHeaderValues(), action, "" + response.getStatus(), res);

        final MediaTypeMatch typeMatch = MediaTypeMatch.find(responseViolations, response, res.getBody(), locator);
        if (typeMatch != null) {
            locator.responseMime(typeMatch.getMatchingMime());
            checkSchema(responseViolations, response.getContent(), typeMatch);
        }
        return typeMatch;
    }

    private void checkSchema(RamlViolations violations, byte[] body, MediaTypeMatch typeMatch) {
        final String schema = typeMatch.getMatchingMime().getSchema();
        if (schema == null) {
            return;
        }
        final SchemaValidator validator = findSchemaValidator(schemaValidators, typeMatch.getTargetType());
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
            validator.validate(new NamedReader(content, new Message("body").toString()), resolveSchema(raml, schema), violations, new Message("schema.body.mismatch", locator, content));
        } catch (UnsupportedEncodingException e) {
            violations.add("charset.invalid", charset);
        }
    }

    private void checkResponseHeaderParameters(Values values, Action action, String responseCode, Response response) {
        responseUsage(usage, action, responseCode).addResponseHeaders(
                new ParameterChecker(responseViolations)
                        .acceptWildcard()
                        .ignoreX(ignoreXheaders)
                        .caseSensitive(false)
                        .predefined(DefaultHeaders.RESPONSE)
                        .checkParameters(response.getHeaders(), values, new Message("headerParam", locator))
        );
    }
}


