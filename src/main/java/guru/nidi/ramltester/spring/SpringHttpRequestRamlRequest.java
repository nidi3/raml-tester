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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlRequest;
import guru.nidi.ramltester.util.UriComponents;
import guru.nidi.ramltester.util.Values;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SpringHttpRequestRamlRequest implements RamlRequest {
    private final HttpRequest request;
    private final byte[] body;
    private final UriComponents uriComponents;

    public SpringHttpRequestRamlRequest(HttpRequest request, byte[] body) {
        this.request = request;
        this.body = body;
        this.uriComponents = UriComponents.fromHttpUrl(request.getURI().toString());
    }

    @Override
    public String getRequestUrl(String baseUri) {
        return (baseUri != null ? baseUri : uriComponents.getServer()) + uriComponents.getPath();
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public Values getQueryValues() {
        return uriComponents.getQueryParameters();
    }

    @Override
    public Values getHeaderValues() {
        final Values headers = new Values();
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            headers.addValues(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    @Override
    public String getContentType() {
        final MediaType contentType = request.getHeaders().getContentType();
        return contentType == null ? null : contentType.toString();
    }

    @Override
    public String getContent() {
        return new String(body, Charset.forName("utf-8"));
    }
}
