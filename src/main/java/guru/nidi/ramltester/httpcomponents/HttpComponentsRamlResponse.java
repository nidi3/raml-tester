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

import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import org.apache.http.HttpResponse;

import static guru.nidi.ramltester.httpcomponents.HttpComponentsUtils.*;

/**
 *
 */
public class HttpComponentsRamlResponse implements RamlResponse {
    private HttpResponse response;

    public HttpComponentsRamlResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getContentType() {
        return contentTypeOf(response);
    }

    @Override
    public byte[] getContent() {
        return contentOf(buffered(response).getEntity());
    }

    @Override
    public Values getHeaderValues() {
        return headerValuesOf(response);
    }
}
