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
    private RamlMethod action;
    private RamlBody requestMime;
    private String responseCode;
    private RamlBody responseMime;

    public Locator() {
    }

    public Locator(RamlResource resource) {
        resource(resource);
    }

    public Locator(RamlMethod action) {
        action(action);
    }

    public Locator(RamlBody mimeType) {
        requestMime(mimeType);
    }

    public void resource(RamlResource resource) {
        this.resource = resource;
        action = null;
        requestMime = null;
        responseCode = null;
        responseMime = null;
    }

    public void action(RamlMethod action) {
        this.resource = action.resource();
        this.action = action;
        requestMime = null;
        responseCode = null;
        responseMime = null;
    }

    public void requestMime(RamlBody requestMime) {
        this.requestMime = requestMime;
        responseCode = null;
        responseMime = null;
    }

    public void responseCode(String responseCode) {
        this.responseCode = responseCode;
        requestMime = null;
        responseMime = null;
    }

    public void responseMime(RamlBody responseMime) {
        this.responseMime = responseMime;
        requestMime = null;
    }

    @Override
    public String toString() {
        if (responseCode != null) {
            return (actionString() + " " + new Message("response", responseCode).toString()) +
                    (responseMime == null ? "" : (" " + new Message("mimeType", responseMime.name()).toString()));
        }
        if (requestMime != null) {
            return (action == null ? "" : (actionString() + " ")) +
                    new Message("mimeType", requestMime.name()).toString();
        }
        if (action != null) {
            return actionString();
        }
        if (resource != null) {
            return new Message("resource", resource.resourcePath()).toString();
        }
        return new Message("root").toString();
    }

    private String actionString() {
        return new Message("action", action.method(), action.resource().resourcePath()).toString();
    }
}
