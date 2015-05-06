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

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FileValue;
import guru.nidi.ramltester.util.IoUtils;
import guru.nidi.ramltester.util.UriComponents;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SpringMockRamlRequest implements RamlRequest {
    private final MockHttpServletRequest delegate;

    public SpringMockRamlRequest(MockHttpServletRequest delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getRequestUrl(String baseUri) {
        return baseUri != null ? baseUri + delegate.getPathInfo() : delegate.getRequestURL().toString();
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public Values getQueryValues() {
        return UriComponents.parseQuery(delegate.getQueryString());
    }

    @Override
    public Values getFormValues() {
        final Values values = new Values(delegate.getParameterMap());
        if (delegate instanceof MockMultipartHttpServletRequest) {
            for (final Map.Entry<String, List<MultipartFile>> entry : ((MockMultipartHttpServletRequest) delegate).getMultiFileMap().entrySet()) {
                for (final MultipartFile file : entry.getValue()) {
                    values.addValue(entry.getKey(), new FileValue());
                }
            }
        }
        return values;
    }

    @Override
    public Values getHeaderValues() {
        final Values headers = new Values();
        final Enumeration<String> names = delegate.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = delegate.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.addValue(name, values.nextElement());
            }
        }
        return headers;
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public byte[] getContent() {
        try {
            return IoUtils.readIntoByteArray(delegate.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Could not read request body", e);
        }
    }
}
