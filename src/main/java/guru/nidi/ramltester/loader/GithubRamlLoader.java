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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class GithubRamlLoader extends UrlRamlLoader {
    private final String resourceBase;

    public GithubRamlLoader(final String token, String project, String resourceBase, CloseableHttpClient httpClient) {
        super("https://api.github.com/repos/" + project + "/contents", new SimpleUrlFetcher() {
            @Override
            protected HttpGet postProcessGet(HttpGet get) {
                if (token != null) {
                    get.addHeader("Authorization", "token " + token);
                }
                return get;
            }
        }, httpClient);
        this.resourceBase = (resourceBase == null || resourceBase.length() == 0) ? "" : (resourceBase + "/");
    }

    public GithubRamlLoader(String token, String project) {
        this(token, project, null, null);
    }

    public GithubRamlLoader(String project) {
        this(null, project, null, null);
    }

    @Override
    public InputStream fetchResource(String name) {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, String> desc = new ObjectMapper().readValue(fetcher.fetchFromUrl(client, base, resourceBase + name), Map.class);
            return fetcher.fetchFromUrl(client, desc.get("download_url"), "");
        } catch (IOException e) {
            throw new ResourceNotFoundException(resourceBase + name, e);
        }
    }

    public static class Factory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "github";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            final int firstSlash = base.indexOf('/');
            final int secondSlash = base.indexOf('/', firstSlash + 1);
            final String project, resourceBase;
            if (secondSlash > 0) {
                project = base.substring(0, secondSlash);
                resourceBase = base.substring(secondSlash + 1);
            } else {
                project = base;
                resourceBase = null;
            }
            return new GithubRamlLoader(username, project, resourceBase, null);
        }
    }
}
