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

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FormDecoder;
import guru.nidi.ramltester.util.UriComponents;

import javax.ws.rs.client.ClientRequestContext;

/**
 *
 */
public class JaxrsContextRamlRequest extends JaxrsContextRamlMessage implements RamlRequest {
    private final ClientRequestContext context;
    private final UriComponents uriComponents;

    public JaxrsContextRamlRequest(ClientRequestContext context) {
        this.context = context;
        this.uriComponents = UriComponents.fromHttpUrl(context.getUri().toString());
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        return (baseUri == null ? uriComponents.getServer() : baseUri) + uriComponents.getPath();
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public Values getQueryValues() {
        return uriComponents.getQueryParameters();
    }

    @Override
    public Values getFormValues() {
        return new FormDecoder().decode(this);
    }

    @Override
    public Values getHeaderValues() {
        return headersOf(context.getHeaders());
    }

    @Override
    public String getContentType() {
        return context.getMediaType() == null ? null : context.getMediaType().toString();
    }

    @Override
    public byte[] getContent() {
        if (context.getEntityStream() instanceof SavingOutputStream) {
            return ((SavingOutputStream) context.getEntityStream()).getSaved();
        }
        throw new IllegalStateException("Request data was not saved");
    }
}
