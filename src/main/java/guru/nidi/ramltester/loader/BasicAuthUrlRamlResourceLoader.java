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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.nio.charset.Charset;

/**
 *
 */
public class BasicAuthUrlRamlResourceLoader extends UrlRamlResourceLoader {
    public BasicAuthUrlRamlResourceLoader(String baseUrl, final String username, final String password, CloseableHttpClient httpClient) {
        super(baseUrl, new SimpleUrlFetcher() {
            @Override
            protected HttpGet postProcessGet(HttpGet get) {
                get.addHeader("Authorization", encode(username, password));
                return get;
            }

            private String encode(String username, String password) {
                return Base64.encodeBase64String(("Basic " + username + ":" + password).getBytes(Charset.forName("iso-8859-1")));
            }
        }, httpClient);
    }

    public BasicAuthUrlRamlResourceLoader(String baseUrl, String username, String password) {
        this(baseUrl, username, password, null);
    }
}
