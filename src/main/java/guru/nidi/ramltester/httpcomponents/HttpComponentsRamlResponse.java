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

import guru.nidi.ramltester.core.RamlResponse;
import guru.nidi.ramltester.util.ParameterValues;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

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
        final Header[] headers = response.getHeaders("Content-Type");
        return (headers == null || headers.length == 0) ? null : headers[0].getValue();
    }

    @Override
    public String getContent() {
        try {
            final HttpEntity entity = response.getEntity();
            final Header encoding = entity.getContentEncoding();
            final String s = EntityUtils.toString(entity, encoding != null ? encoding.getValue() : "UTF-8");
            final StringEntity buffered = new StringEntity(s);
            buffered.setChunked(entity.isChunked());
            buffered.setContentEncoding(entity.getContentEncoding());
            buffered.setContentType(entity.getContentType());
            response.setEntity(buffered);
            return s;
        } catch (IOException e) {
            throw new RuntimeException("Could not get response content", e);
        }
    }

    @Override
    public Map<String, String[]> getHeaderMap() {
        ParameterValues headers = new ParameterValues();
        for (Header header : response.getAllHeaders()) {
            headers.addValue(header.getName(), header.getValue());
        }
        return headers.getValues();
    }
}
