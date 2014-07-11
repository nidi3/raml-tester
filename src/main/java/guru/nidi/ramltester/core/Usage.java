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

import java.util.Collections;
import java.util.Set;

/**
 *
 */
class Usage {
    private String path;
    private Set<String> queryParameters = Collections.emptySet();
    private Set<String> formParameters = Collections.emptySet();
    private Set<String> requestHeaders = Collections.emptySet();
    private Set<String> responseHeaders = Collections.emptySet();
    private String responseCode;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Set<String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Set<String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Set<String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Set<String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Set<String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public Set<String> getFormParameters() {
        return formParameters;
    }

    public void setFormParameters(Set<String> formParameters) {
        this.formParameters = formParameters;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "Usage{" +
                "path='" + path + '\'' +
                ", queryParameters=" + queryParameters +
                ", formParameters=" + formParameters +
                ", requestHeaders=" + requestHeaders +
                ", responseHeaders=" + responseHeaders +
                ", responseCode='" + responseCode + '\'' +
                '}';
    }
}
