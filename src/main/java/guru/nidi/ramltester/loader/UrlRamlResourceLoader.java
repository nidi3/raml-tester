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
package guru.nidi.ramltester.loader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class UrlRamlResourceLoader implements RamlResourceLoader {
    private final String base;
    protected final CloseableHttpClient client;

    public UrlRamlResourceLoader(String baseUrl, CloseableHttpClient httpClient) {
        this.base = baseUrl;
        this.client = httpClient == null
                ? HttpClientBuilder.create().build()
                : httpClient;
    }

    public UrlRamlResourceLoader(String baseUrl) {
        this(baseUrl, null);
    }

    @Override
    public InputStream fetchResource(String name) {
        try {
            final HttpGet get = postProcessGet(new HttpGet(base + "/" + encode(name)));
            final CloseableHttpResponse getResult = client.execute(get);
            if (getResult.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ResourceNotFoundException(name, "Http response status not ok: " + getResult.getStatusLine().toString());
            }
            return getResult.getEntity().getContent();
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    private String encode(String name) {
        return name.replace(" ", "%20");
    }

    protected HttpGet postProcessGet(HttpGet get) {
        return get;
    }
}
