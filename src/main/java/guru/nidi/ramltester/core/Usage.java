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
package guru.nidi.ramltester.core;

import java.util.*;

public class Usage implements Iterable<Map.Entry<String, Usage.Resource>> {
    private final Map<String, Resource> resources = new HashMap<>();

    private static <T> T getOrCreate(Class<T> clazz, Map<String, T> map, String name) {
        T res = map.get(name);
        if (res == null) {
            try {
                res = clazz.newInstance();
                map.put(name, res);
            } catch (Exception e) {
                throw new RamlCheckerException("Could not create instance of " + clazz, e);
            }
        }
        return res;
    }

    public Resource resource(String path) {
        return getOrCreate(Resource.class, resources, path);
    }

    public void add(Usage usage) {
        for (final Map.Entry<String, Resource> resourceEntry : usage) {
            final Resource resource = resource(resourceEntry.getKey());
            resource.incUses(resourceEntry.getValue().getUses());
            for (final Map.Entry<String, Method> methodEntry : resourceEntry.getValue()) {
                final Method method = resource.method(methodEntry.getKey());
                final Method usageMethod = methodEntry.getValue();
                method.incUses(usageMethod.getUses());
                method.addQueryParameters(usageMethod.getQueryParameters());
                method.addRequestHeaders(usageMethod.getRequestHeaders());
                method.addResponseCodes(usageMethod.getResponseCodes());
                for (final Map.Entry<String, Response> responseEntry : usageMethod.responses()) {
                    final Response response = method.response(responseEntry.getKey());
                    response.addResponseHeaders(responseEntry.getValue().getResponseHeaders());
                }
                for (final Map.Entry<String, Body> bodyEntry : usageMethod.bodies()) {
                    final Body body = method.body(bodyEntry.getKey());
                    body.addParameters(bodyEntry.getValue().getParameters());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Usage" + resources;
    }

    @Override
    public Iterator<Map.Entry<String, Resource>> iterator() {
        return resources.entrySet().iterator();
    }

    public Set<String> getUnusedResources() {
        final Set<String> res = new HashSet<>();
        for (final Map.Entry<String, Resource> resourceEntry : this) {
            if (resourceEntry.getValue().getUses() == 0 && !resourceEntry.getValue().methods.isEmpty()) {
                res.add(resourceEntry.getKey());
            }
        }
        return res;
    }

    public Set<String> getUnusedMethods() {
        return UsageCollector.METHOD.collect(this);
    }

    public Set<String> getUnusedQueryParameters() {
        return UsageCollector.QUERY_PARAM.collect(this);
    }

    public Set<String> getUnusedBodyParameters() {
        return UsageCollector.BODY_PARAM.collect(this);
    }

    public Set<String> getUnusedRequestHeaders() {
        return UsageCollector.REQUEST_HEADER.collect(this);
    }

    public Set<String> getUnusedResponseHeaders() {
        return UsageCollector.RESPONSE_HEADER.collect(this);
    }

    public Set<String> getUnusedResponseCodes() {
        return UsageCollector.RESPONSE_CODE.collect(this);
    }

    static class UsageBase {
        private int uses;

        public void incUses(int count) {
            uses += count;
        }

        public int getUses() {
            return uses;
        }
    }

    static class Resource extends UsageBase implements Iterable<Map.Entry<String, Method>> {
        private final Map<String, Method> methods = new HashMap<>();

        public Method method(String name) {
            return getOrCreate(Method.class, methods, name);
        }

        @Override
        public String toString() {
            return "Resource" + methods;
        }

        @Override
        public Iterator<Map.Entry<String, Method>> iterator() {
            return methods.entrySet().iterator();
        }
    }

    static class Method extends UsageBase {
        private final Map<String, Response> responses = new HashMap<>();
        private final Map<String, Body> bodies = new HashMap<>();
        private final CountSet<String> queryParameters = new CountSet<>();
        private final CountSet<String> requestHeaders = new CountSet<>();
        private final CountSet<String> responseCodes = new CountSet<>();

        public Response response(String name) {
            return getOrCreate(Response.class, responses, name);
        }

        public Iterable<Map.Entry<String, Response>> responses() {
            return responses.entrySet();
        }

        public Body body(String name) {
            return getOrCreate(Body.class, bodies, name);
        }

        public Iterable<Map.Entry<String, Body>> bodies() {
            return bodies.entrySet();
        }

        public void addQueryParameters(Collection<String> names) {
            queryParameters.addAll(names);
        }

        public void initQueryParameters(Collection<String> names) {
            queryParameters.addAll(names, 0);
        }

        public void addRequestHeaders(Collection<String> names) {
            requestHeaders.addAll(names);
        }

        public void initRequestHeaders(Collection<String> names) {
            requestHeaders.addAll(names, 0);
        }

        public void addResponseCode(String name) {
            responseCodes.add(name);
        }

        public void addResponseCodes(Collection<String> names) {
            responseCodes.addAll(names);
        }

        public void initResponseCodes(Collection<String> names) {
            responseCodes.addAll(names, 0);
        }

        public CountSet<String> getQueryParameters() {
            return queryParameters;
        }

        public CountSet<String> getRequestHeaders() {
            return requestHeaders;
        }

        public CountSet<String> getResponseCodes() {
            return responseCodes;
        }

        @Override
        public String toString() {
            return "Method{"
                    + "responses=" + responses
                    + ", bodies=" + bodies
                    + ", queryParameters=" + queryParameters
                    + ", requestHeaders=" + requestHeaders
                    + ", responseCodes=" + responseCodes
                    + '}';
        }
    }

    static class Response {
        private final CountSet<String> responseHeaders = new CountSet<>();

        public void addResponseHeaders(Set<String> names) {
            responseHeaders.addAll(names);
        }

        public void initResponseHeaders(Collection<String> names) {
            responseHeaders.addAll(names, 0);
        }

        public CountSet<String> getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public String toString() {
            return "Response{"
                    + "responseHeaders=" + responseHeaders
                    + '}';
        }
    }

    static class Body {
        private final CountSet<String> parameters = new CountSet<>();

        public void addParameters(Set<String> names) {
            parameters.addAll(names);
        }

        public void initParameters(Collection<String> names) {
            parameters.addAll(names, 0);
        }

        public CountSet<String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "Body{"
                    + "parameters=" + parameters
                    + '}';
        }
    }
}
