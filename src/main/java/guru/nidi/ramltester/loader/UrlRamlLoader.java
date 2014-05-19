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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class UrlRamlLoader implements RamlLoader {
    private final String base;
    private final CloseableHttpClient client;
    private final UrlFetcher fetcher;

    public UrlRamlLoader(String base, UrlFetcher fetcher, CloseableHttpClient httpClient) {
        this.base = base;
        this.fetcher = fetcher;
        this.client = httpClient == null
                ? HttpClientBuilder.create().build()
                : httpClient;
    }

    public UrlRamlLoader(String base, UrlFetcher fetcher) {
        this(base, fetcher, null);
    }

    public UrlRamlLoader(String baseUrl) {
        this(baseUrl, new SimpleUrlFetcher(), null);
    }

    @Override
    public InputStream fetchResource(String name) {
        try {
            return fetcher.fetchFromUrl(client, base, name);
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }
}
