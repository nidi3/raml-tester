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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class SimpleUrlFetcher implements UrlFetcher {
    @Override
    public InputStream fetchFromUrl(CloseableHttpClient client, String base, String name) throws IOException {
        final HttpGet get = postProcessGet(new HttpGet(base + "/" + encodeUrl(name)));
        final CloseableHttpResponse getResult = client.execute(get);
        if (getResult.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Http response status not ok: " + getResult.getStatusLine().toString());
        }
        return getResult.getEntity().getContent();
    }

    protected String encodeUrl(String name) {
        return name.replace(" ", "%20");
    }

    protected HttpGet postProcessGet(HttpGet get) {
        return get;
    }

}
