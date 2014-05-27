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
import guru.nidi.ramltester.util.Values;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class SpringMockRamlResponse implements RamlResponse {
    private final MockHttpServletResponse delegate;

    public SpringMockRamlResponse(MockHttpServletResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getStatus() {
        return delegate.getStatus();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContent() {
        try {
            return delegate.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Problem extracting response content", e);
        }
    }

    @Override
    public Values getHeaderValues() {
        final Values headers = new Values();
        for (String name : delegate.getHeaderNames()) {
            headers.addValues(name, delegate.getHeaders(name));
        }
        return headers;
    }
}
