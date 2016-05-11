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
package guru.nidi.ramltester.core;

import java.util.*;

/**
 *
 */
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
            for (final Map.Entry<String, Action> actionEntry : resourceEntry.getValue()) {
                final Action action = resource.action(actionEntry.getKey());
                final Action usageAction = actionEntry.getValue();
                action.incUses(usageAction.getUses());
                action.addQueryParameters(usageAction.getQueryParameters());
                action.addRequestHeaders(usageAction.getRequestHeaders());
                action.addResponseCodes(usageAction.getResponseCodes());
                for (final Map.Entry<String, Response> responseEntry : usageAction.responses()) {
                    final Response response = action.response(responseEntry.getKey());
                    response.addResponseHeaders(responseEntry.getValue().getResponseHeaders());
                }
                for (final Map.Entry<String, MimeType> mimeTypeEntry : usageAction.mimeTypes()) {
                    final MimeType mimeType = action.mimeType(mimeTypeEntry.getKey());
                    mimeType.addFormParameters(mimeTypeEntry.getValue().getFormParameters());
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
            if (resourceEntry.getValue().getUses() == 0 && !resourceEntry.getValue().actions.isEmpty()) {
                res.add(resourceEntry.getKey());
            }
        }
        return res;
    }

    public Set<String> getUnusedActions() {
        return UsageCollector.ACTION.collect(this);
    }

    public Set<String> getUnusedQueryParameters() {
        return UsageCollector.QUERY_PARAM.collect(this);
    }

    public Set<String> getUnusedFormParameters() {
        return UsageCollector.FORM_PARAM.collect(this);
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

    static class Resource extends UsageBase implements Iterable<Map.Entry<String, Action>> {
        private final Map<String, Action> actions = new HashMap<>();

        public Action action(String name) {
            return getOrCreate(Action.class, actions, name);
        }

        @Override
        public String toString() {
            return "Resource" + actions;
        }

        @Override
        public Iterator<Map.Entry<String, Action>> iterator() {
            return actions.entrySet().iterator();
        }
    }

    static class Action extends UsageBase {
        private final Map<String, Response> responses = new HashMap<>();
        private final Map<String, MimeType> mimeTypes = new HashMap<>();
        private final CountSet<String> queryParameters = new CountSet<>();
        private final CountSet<String> requestHeaders = new CountSet<>();
        private final CountSet<String> responseCodes = new CountSet<>();

        public Response response(String name) {
            return getOrCreate(Response.class, responses, name);
        }

        public Iterable<Map.Entry<String, Response>> responses() {
            return responses.entrySet();
        }

        public MimeType mimeType(String name) {
            return getOrCreate(MimeType.class, mimeTypes, name);
        }

        public Iterable<Map.Entry<String, MimeType>> mimeTypes() {
            return mimeTypes.entrySet();
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
            return "Action{" +
                    "responses=" + responses +
                    ", mimeTypes=" + mimeTypes +
                    ", queryParameters=" + queryParameters +
                    ", requestHeaders=" + requestHeaders +
                    ", responseCodes=" + responseCodes +
                    '}';
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
            return "Response{" +
                    "responseHeaders=" + responseHeaders +
                    '}';
        }
    }

    static class MimeType {
        private final CountSet<String> formParameters = new CountSet<>();

        public void addFormParameters(Set<String> names) {
            formParameters.addAll(names);
        }

        public void initFormParameters(Collection<String> names) {
            formParameters.addAll(names, 0);
        }

        public CountSet<String> getFormParameters() {
            return formParameters;
        }

        @Override
        public String toString() {
            return "MimeType{" +
                    "formParameters=" + formParameters +
                    '}';
        }
    }
}
