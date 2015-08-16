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

import guru.nidi.ramltester.util.Message;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;

/**
 *
 */
class Locator {
    private Resource resource;
    private Action action;
    private MimeType requestMime;
    private String responseCode;
    private MimeType responseMime;

    public Locator() {
    }

    public Locator(Resource resource) {
        resource(resource);
    }

    public Locator(Action action) {
        action(action);
    }

    public Locator(MimeType mimeType) {
        requestMime(mimeType);
    }

    public void resource(Resource resource) {
        this.resource = resource;
        action = null;
        requestMime = null;
        responseCode = null;
        responseMime = null;
    }

    public void action(Action action) {
        this.resource = action.getResource();
        this.action = action;
        requestMime = null;
        responseCode = null;
        responseMime = null;
    }

    public void requestMime(MimeType requestMime) {
        this.requestMime = requestMime;
        responseCode = null;
        responseMime = null;
    }

    public void responseCode(String responseCode) {
        this.responseCode = responseCode;
        requestMime = null;
        responseMime = null;
    }

    public void responseMime(MimeType responseMime) {
        this.responseMime = responseMime;
        requestMime = null;
    }

    @Override
    public String toString() {
        if (responseCode != null) {
            return (actionString() + " " + new Message("response", responseCode).toString()) +
                    (responseMime == null ? "" : (" " + new Message("mimeType", responseMime.getType()).toString()));
        }
        if (requestMime != null) {
            return (action == null ? "" : (actionString() + " ")) +
                    new Message("mimeType", requestMime.getType()).toString();
        }
        if (action != null) {
            return actionString();
        }
        if (resource != null) {
            return new Message("resource", resource.getUri()).toString();
        }
        return new Message("root").toString();
    }

    private String actionString() {
        return new Message("action", action.getType(), action.getResource().getUri()).toString();
    }
}
