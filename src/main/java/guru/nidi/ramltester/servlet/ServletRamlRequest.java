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
package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.core.RamlRequest;
import guru.nidi.ramltester.util.Values;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;

/**
 *
 */
public class ServletRamlRequest extends HttpServletRequestWrapper implements RamlRequest {
    public ServletRamlRequest(HttpServletRequest delegate) {
        super(delegate);
    }

    private HttpServletRequest request() {
        return (HttpServletRequest) getRequest();
    }

    @Override
    public String getRequestUrl(String baseUri) {
        return baseUri != null ? (baseUri + request().getPathInfo()) : request().getRequestURL().toString();
    }

    @Override
    public Values getQueryValues() {
        return new Values(getParameterMap());
    }

    @Override
    public Values getHeaderValues() {
        final Values headers = new Values();
        final Enumeration<String> names = request().getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = request().getHeaders(name);
            while (values.hasMoreElements()) {
                headers.addValue(name, values.nextElement());
            }
        }
        return headers;
    }

    @Override
    public String getContent() {
        return null;
    }
}
