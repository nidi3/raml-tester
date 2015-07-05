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
package guru.nidi.ramltester.jaxrs;

import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.IoUtils;

import javax.ws.rs.client.ClientResponseContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 */
public class JaxrsResponseContextRamlResponse implements RamlResponse {
    private final ClientResponseContext context;
    private final byte[] content;

    public JaxrsResponseContextRamlResponse(ClientResponseContext context) {
        this.context = context;
        try {
            content = IoUtils.readIntoByteArray(context.getEntityStream());
            context.setEntityStream(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new RuntimeException("Could not get response content", e);
        }
    }

    @Override
    public int getStatus() {
        return context.getStatus();
    }

    @Override
    public Values getHeaderValues() {
        return JaxrsUtils.headersOf(context.getHeaders());
    }

    @Override
    public String getContentType() {
        return context.getMediaType() == null ? null : context.getMediaType().toString();
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
