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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SpringClientHttpResponseRamlResponse implements ClientHttpResponse, RamlResponse {
    private final ClientHttpResponse response;
    private final String encoding;
    private byte[] body;

    public SpringClientHttpResponseRamlResponse(ClientHttpResponse response, String encoding) {
        this.response = response;
        this.encoding = encoding;
    }

    public SpringClientHttpResponseRamlResponse(ClientHttpResponse response) {
        this(response, "utf-8");
    }

    @Override
    public int getStatus() {
        try {
            return getRawStatusCode();
        } catch (IOException e) {
            throw new RuntimeException("Problem getting status", e);
        }
    }

    @Override
    public String getContentType() {
        final MediaType contentType = getHeaders().getContentType();
        return contentType == null ? null : contentType.toString();
    }

    @Override
    public String getContent() {
        try {
            char[] buf = new char[getBody().available()];
            final InputStreamReader reader = new InputStreamReader(getBody(), encoding);
            final int read = reader.read(buf);
            return new String(buf, 0, read);
        } catch (IOException e) {
            throw new RuntimeException("Problem getting content", e);
        }
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (body == null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(response.getBody(), out);
            body = out.toByteArray();
        }
        return new ByteArrayInputStream(body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public Map<String, String[]> getHeaderMap() {
        final HashMap<String, String[]> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
            headers.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return headers;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1000];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }
}
