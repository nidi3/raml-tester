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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
abstract class UsageCollector {
    static final UsageCollector ACTION = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            if (action.getUses() == 0) {
                result.add(key);
            }
        }
    };

    static final UsageCollector QUERY_PARAM = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            for (final Map.Entry<String, Integer> queryEntry : action.getQueryParameters().values()) {
                if (queryEntry.getValue() == 0) {
                    add(result, queryEntry.getKey(), key);
                }
            }
        }
    };

    static final UsageCollector FORM_PARAM = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            for (final Map.Entry<String, Usage.MimeType> mimeTypeEntry : action.mimeTypes()) {
                for (final Map.Entry<String, Integer> formEntry : mimeTypeEntry.getValue().getFormParameters().values()) {
                    if (formEntry.getValue() == 0) {
                        add(result, formEntry.getKey(), key + " (" + mimeTypeEntry.getKey() + ")");
                    }
                }
            }
        }
    };

    static final UsageCollector REQUEST_HEADER = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            for (final Map.Entry<String, Integer> requestEntry : action.getRequestHeaders().values()) {
                if (requestEntry.getValue() == 0) {
                    add(result, requestEntry.getKey(), key);
                }
            }
        }
    };

    static final UsageCollector RESPONSE_HEADER = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            for (final Map.Entry<String, Usage.Response> responseEntry : action.responses()) {
                for (final Map.Entry<String, Integer> headerEntry : responseEntry.getValue().getResponseHeaders().values()) {
                    if (headerEntry.getValue() == 0) {
                        add(result, headerEntry.getKey(), key + " -> " + responseEntry.getKey());
                    }
                }
            }
        }
    };

    static final UsageCollector RESPONSE_CODE = new UsageCollector() {
        @Override
        public void collect(String key, Usage.Action action, Set<String> result) {
            for (final Map.Entry<String, Integer> responseCodeEntry : action.getResponseCodes().values()) {
                if (responseCodeEntry.getValue() == 0) {
                    add(result, responseCodeEntry.getKey(), key);
                }
            }
        }
    };

    private UsageCollector() {
    }

    abstract void collect(String key, Usage.Action action, Set<String> result);

    public Set<String> collect(Usage usage) {
        final Set<String> res = new HashSet<String>();
        for (final Map.Entry<String, Usage.Resource> resourceEntry : usage) {
            for (final Map.Entry<String, Usage.Action> actionEntry : resourceEntry.getValue()) {
                collect(actionEntry.getKey() + " " + resourceEntry.getKey(), actionEntry.getValue(), res);
            }
        }
        return res;
    }

    protected static void add(Set<String> result, String key, String value) {
        result.add(key + " in " + value);
    }

}
