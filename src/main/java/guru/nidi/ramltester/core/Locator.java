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

import guru.nidi.ramltester.model.internal.RamlBody;
import guru.nidi.ramltester.model.internal.RamlMethod;
import guru.nidi.ramltester.model.internal.RamlResource;
import guru.nidi.ramltester.util.Message;

final class Locator {
    private RamlResource resource;
    private RamlMethod method;
    private RamlBody requestBody;
    private String responseCode;
    private RamlBody responseBody;

    public Locator() {
    }

    public Locator(RamlResource resource) {
        resource(resource);
    }

    public Locator(RamlMethod method) {
        method(method);
    }

    public Locator(RamlBody body) {
        requestBody(body);
    }

    public void resource(RamlResource resource) {
        this.resource = resource;
        method = null;
        requestBody = null;
        responseCode = null;
        responseBody = null;
    }

    public void method(RamlMethod method) {
        this.resource = method.resource();
        this.method = method;
        requestBody = null;
        responseCode = null;
        responseBody = null;
    }

    public void requestBody(RamlBody requestBody) {
        this.requestBody = requestBody;
        responseCode = null;
        responseBody = null;
    }

    public void responseCode(String responseCode) {
        this.responseCode = responseCode;
        requestBody = null;
        responseBody = null;
    }

    public void responseBody(RamlBody responseBody) {
        this.responseBody = responseBody;
        requestBody = null;
    }

    @Override
    public String toString() {
        if (responseCode != null) {
            return (methodString() + " " + new Message("response", responseCode).toString()) +
                    (responseBody == null ? "" : (" " + new Message("mimeType", responseBody.name()).toString()));
        }
        if (requestBody != null) {
            return (method == null ? "" : (methodString() + " ")) +
                    new Message("mimeType", requestBody.name()).toString();
        }
        if (method != null) {
            return methodString();
        }
        if (resource != null) {
            return new Message("resource", resource.resourcePath()).toString();
        }
        return new Message("root").toString();
    }

    private String methodString() {
        return new Message("action", method.method(), method.resource().resourcePath()).toString();
    }
}
