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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

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
    public String getRequestUrl(String servletUri) {
        return servletUri != null ? (servletUri + request().getPathInfo()) : request().getRequestURL().toString();
    }

    @Override
    public Map<String, String[]> getHeaderMap() {
        final HashMap<String, String[]> headers = new HashMap<>();
        final Enumeration<String> names = request().getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = request().getHeaders(name);
            final List<String> valueList = new ArrayList<>();
            while (values.hasMoreElements()) {
                valueList.add(values.nextElement());
            }
            headers.put(name, valueList.toArray(new String[valueList.size()]));
        }
        return headers;
    }

    @Override
    public String getContent() {
        return null;
    }
}
