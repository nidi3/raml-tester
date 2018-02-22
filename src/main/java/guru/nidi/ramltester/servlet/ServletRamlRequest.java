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
package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Enumeration;

/**
 *
 */
public class ServletRamlRequest extends HttpServletRequestWrapper implements RamlRequest {
    private byte[] content;

    public ServletRamlRequest(HttpServletRequest delegate) {
        super(delegate);
    }

    private HttpServletRequest request() {
        return (HttpServletRequest) getRequest();
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        final String servletPath = includeServletPath ? request().getServletPath() : "";
        final String pathInfo = request().getPathInfo() == null ? "" : request().getPathInfo();
        return baseUri == null
                ? request().getRequestURL().toString()
                : (baseUri + servletPath + pathInfo);
    }

    @Override
    public Values getQueryValues() {
        return UriComponents.parseQuery(request().getQueryString());
    }

    @Override
    public Values getFormValues() {
        return new FormDecoder().decode(this);
    }

    @Override
    public Values getHeaderValues() {
        return getHeaderValues(request());
    }

    public static Values getHeaderValues(HttpServletRequest request) {
        final Values headers = new Values();
        final Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.addValue(name, values.nextElement());
            }
        }
        return headers;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        readContentIfNeeded();
        return new DelegatingServletInputStream(new ByteArrayInputStream(content));
    }


    @Override
    public BufferedReader getReader() throws IOException {
        readContentIfNeeded();
        final InputStreamReader in = getCharacterEncoding() == null
                ? new InputStreamReader(new ByteArrayInputStream(content))
                : new InputStreamReader(new ByteArrayInputStream(content), getCharacterEncoding());
        return new BufferedReader(in);
    }

    @Override
    public byte[] getContent() {
        try {
            readContentIfNeeded();
            return IoUtils.readIntoByteArray(getInputStream());
        } catch (IOException e) {
            throw new RamlCheckerException("Could not read content", e);
        }
    }

    private void readContentIfNeeded() throws IOException {
        if (content == null) {
            content = IoUtils.readIntoByteArray(super.getInputStream());
        }
    }
}
