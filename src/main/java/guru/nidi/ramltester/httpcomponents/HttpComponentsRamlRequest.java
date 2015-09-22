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
package guru.nidi.ramltester.httpcomponents;

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FormDecoder;
import guru.nidi.ramltester.util.UriComponents;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

/**
 *
 */
public class HttpComponentsRamlRequest extends HttpComponentsRamlMessage implements RamlRequest {
    private final String path;
    private final String url;
    private final HttpRequest request;

    public HttpComponentsRamlRequest(HttpHost host, HttpRequest request) {
        this.request = request;
        path = UriComponents.fromHttpUrl(request.getRequestLine().getUri()).getPath();
        url = host.toString() + request.getRequestLine().getUri();
    }

    public HttpComponentsRamlRequest(HttpUriRequest request) {
        this.request = request;
        path = request.getURI().getPath();
        url = request.getURI().toString();
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        return baseUri == null ? url : (baseUri + path);
    }

    @Override
    public String getMethod() {
        return request.getRequestLine().getMethod();
    }

    @Override
    public Values getQueryValues() {
        final UriComponents uriComponents = UriComponents.fromHttpUrl(request.getRequestLine().getUri());
        return uriComponents.getQueryParameters();
    }

    @Override
    public Values getFormValues() {
        return new FormDecoder().decode(this);
    }

    @Override
    public Values getHeaderValues() {
        return headerValuesOf(request);
    }

    @Override
    public String getContentType() {
        return contentTypeOf(request);
    }

    @Override
    public byte[] getContent() {
        return (request instanceof HttpEntityEnclosingRequest)
                ? contentOf(buffered((HttpEntityEnclosingRequest) request).getEntity())
                : null;
    }
}
