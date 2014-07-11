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

import org.raml.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RamlCoverage {
    private final Set<String> unusedPaths;
    private final Set<String> unusedQueryParameters;
    private final Set<String> unusedFormParameters;
    private final Set<String> unusedRequestHeaders;
    private final Set<String> unusedResponseHeaders;
    private final Set<String> unusedResponseCodes;

    public RamlCoverage(Raml raml, List<RamlReport> reports) {
        final Map<String, Resource> resources = raml.getResources();
        unusedPaths = pathsOf(resources);
        unusedQueryParameters = queryParametersOf(resources);
        unusedFormParameters = formParametersOf(resources);
        unusedRequestHeaders = requestHeadersOf(resources);
        unusedResponseHeaders = responseHeadersOf(resources);
        unusedResponseCodes = responseCodesOf(resources);
        for (RamlReport report : reports) {
            addUsage(report);
        }
    }

    private Set<String> queryParametersOf(Map<String, Resource> resources) {
        Set<String> query = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                query.addAll(action.getQueryParameters().keySet());
            }
            query.addAll(queryParametersOf(entry.getValue().getResources()));
        }
        return query;
    }

    private Set<String> formParametersOf(Map<String, Resource> resources) {
        Set<String> form = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                if (action.getBody() != null) {
                    for (MimeType mimeType : action.getBody().values()) {
                        if (mimeType.getFormParameters() != null) {
                            form.addAll(mimeType.getFormParameters().keySet());
                        }
                    }
                }
            }
            form.addAll(formParametersOf(entry.getValue().getResources()));
        }
        return form;
    }

    private Set<String> responseCodesOf(Map<String, Resource> resources) {
        Set<String> form = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                form.addAll(action.getResponses().keySet());
            }
            form.addAll(responseCodesOf(entry.getValue().getResources()));
        }
        return form;
    }

    private Set<String> requestHeadersOf(Map<String, Resource> resources) {
        Set<String> headers = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                headers.addAll(action.getHeaders().keySet());
            }
            headers.addAll(requestHeadersOf(entry.getValue().getResources()));
        }
        return headers;
    }

    private Set<String> responseHeadersOf(Map<String, Resource> resources) {
        Set<String> headers = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                for (Response response : action.getResponses().values()) {
                    headers.addAll(response.getHeaders().keySet());
                }
            }
            headers.addAll(responseHeadersOf(entry.getValue().getResources()));
        }
        return headers;
    }

    private Set<String> pathsOf(Map<String, Resource> resources) {
        Set<String> paths = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            paths.add(entry.getValue().getUri());
            paths.addAll(pathsOf(entry.getValue().getResources()));
        }
        return paths;
    }

    private void addUsage(RamlReport report) {
        final Usage usage = report.getUsage();
        unusedPaths.remove(usage.getPath());
        unusedQueryParameters.removeAll(usage.getQueryParameters());
        unusedFormParameters.removeAll(usage.getFormParameters());
        unusedRequestHeaders.removeAll(usage.getRequestHeaders());
        unusedResponseHeaders.removeAll(usage.getResponseHeaders());
        unusedResponseCodes.remove(usage.getResponseCode());
    }

    public Set<String> getUnusedPaths() {
        return unusedPaths;
    }

    public Set<String> getUnusedQueryParameters() {
        return unusedQueryParameters;
    }

    public Set<String> getUnusedFormParameters() {
        return unusedFormParameters;
    }

    public Set<String> getUnusedRequestHeaders() {
        return unusedRequestHeaders;
    }

    public Set<String> getUnusedResponseHeaders() {
        return unusedResponseHeaders;
    }

    public Set<String> getUnusedResponseCodes() {
        return unusedResponseCodes;
    }

    @Override
    public String toString() {
        return "RamlCoverage{" +
                "unusedPaths=" + unusedPaths +
                ", unusedQueryParameters=" + unusedQueryParameters +
                ", unusedFormParameters=" + unusedFormParameters +
                ", unusedRequestHeaders=" + unusedRequestHeaders +
                ", unusedResponseHeaders=" + unusedResponseHeaders +
                ", unusedResponseCodes=" + unusedResponseCodes +
                '}';
    }
}
