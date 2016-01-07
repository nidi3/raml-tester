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
package guru.nidi.ramltester.httpcomponents;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.IoUtils;
import org.apache.http.*;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;

/**
 *
 */
class HttpComponentsRamlMessage {
    protected String contentTypeOf(HttpMessage message) {
        final Header contentType = message.getFirstHeader("Content-Type");
        return contentType == null ? null : contentType.getValue();
    }

    protected Values headerValuesOf(HttpMessage message) {
        final Values headers = new Values();
        for (final Header header : message.getAllHeaders()) {
            headers.addValue(header.getName(), header.getValue());
        }
        return headers;
    }

    protected HttpResponse buffered(HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if (entity != null && !entity.isRepeatable()) {
            response.setEntity(buffered(entity));
        }
        return response;
    }

    protected HttpEntityEnclosingRequest buffered(HttpEntityEnclosingRequest request) {
        final HttpEntity entity = request.getEntity();
        if (!entity.isRepeatable()) {
            request.setEntity(buffered(entity));
        }
        return request;
    }

    private BufferedHttpEntity buffered(HttpEntity entity) {
        try {
            return new BufferedHttpEntity(entity);
        } catch (IOException e) {
            throw new RamlCheckerException("Could not read content of entity", e);
        }
    }

    protected byte[] contentOf(HttpEntity entity) {
        try {
            return entity == null ? null : IoUtils.readIntoByteArray(entity.getContent());
        } catch (IOException e) {
            throw new RamlCheckerException("Could not get response content", e);
        }
    }
}
