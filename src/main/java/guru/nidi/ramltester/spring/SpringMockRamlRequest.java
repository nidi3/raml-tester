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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.util.FileValue;
import guru.nidi.ramltester.util.IoUtils;
import guru.nidi.ramltester.util.UriComponents;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class SpringMockRamlRequest implements RamlRequest {
    private final MockHttpServletRequest delegate;

    public SpringMockRamlRequest(MockHttpServletRequest delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        final String servletPath = includeServletPath ? delegate.getServletPath() : "";
        final String pathInfo = delegate.getPathInfo() == null ? "" : delegate.getPathInfo();
        return baseUri == null
                ? delegate.getRequestURL().toString()
                : (baseUri + servletPath + pathInfo);
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public Values getQueryValues() {
        final String q = delegate.getQueryString();
        try {
            return UriComponents.parseQuery(q == null ? null : UriUtils.decode(q, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Values getFormValues() {
        final Values values = new Values(delegate.getParameterMap());
        if (delegate instanceof MockMultipartHttpServletRequest) {
            for (final Map.Entry<String, List<MultipartFile>> entry : ((MockMultipartHttpServletRequest) delegate).getMultiFileMap().entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    values.addValue(entry.getKey(), new FileValue());
                }
            }
        }
        return values;
    }

    @Override
    public Values getHeaderValues() {
        return ServletRamlRequest.getHeaderValues(delegate);
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
            throw new RamlCheckerException("Could not read request body", e);
        }
    }
}
