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

import guru.nidi.ramltester.core.RamlResponse;
import guru.nidi.ramltester.util.IoUtils;
import guru.nidi.ramltester.util.Values;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

import static guru.nidi.ramltester.spring.SpringUtils.contentTypeOf;
import static guru.nidi.ramltester.spring.SpringUtils.headerValuesOf;

/**
 *
 */
public class SpringClientHttpResponseRamlResponse implements ClientHttpResponse, RamlResponse {
    private final ClientHttpResponse response;
    private final String encoding;

    public SpringClientHttpResponseRamlResponse(ClientHttpResponse response, String encoding) {
        this.response = response;
        this.encoding = encoding;
    }

    public SpringClientHttpResponseRamlResponse(ClientHttpResponse response) {
        this(response, "utf-8");
    }

    @Override
    public int getStatus() {
        try {
            return getRawStatusCode();
        } catch (IOException e) {
            throw new RuntimeException("Problem getting status", e);
        }
    }

    @Override
    public String getContentType() {
        return contentTypeOf(getHeaders());
    }

    @Override
    public byte[] getContent() {
        try {
            return IoUtils.readIntoByteArray(getBody());
        } catch (IOException e) {
            throw new RuntimeException("Problem getting content", e);
        }
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        return response.getBody();
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public Values getHeaderValues() {
        return headerValuesOf(response.getHeaders());
    }
}
