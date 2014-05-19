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
package guru.nidi.ramltester.apidesigner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramltester.loader.FormLoginUrlFetcher;
import guru.nidi.ramltester.loader.SimpleUrlFetcher;
import guru.nidi.ramltester.loader.UrlRamlResourceLoader;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 */
public class ApiPortalLoader extends UrlRamlResourceLoader {
    private final ApiFilesResponse response;
    private final Class<? extends ApiFilesResponse> responseClass;

    public ApiPortalLoader(String user, String password) throws IOException {
        super("http://api-portal.anypoint.mulesoft.com",
                new FormLoginUrlFetcher("rest/raml/v1", "ajax/apihub/login-register/form?section=login", user, password, "name", "pass") {
                    @Override
                    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
                        parameters.add(new BasicNameValuePair("form_id", "user_login"));
                    }
                }
        );
        this.responseClass = ApiPortalFilesResponse.class;
        this.response = load();
    }

    public ApiPortalLoader(String baseUrl) throws IOException {
        super(baseUrl, new SimpleUrlFetcher());
        this.responseClass = ApiDesignerFilesResponse.class;
        this.response = load();
    }

    @Override
    public InputStream fetchResource(String resourceName) {
        final ApiFile file = findFile(resourceName);
        if (file == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        return new ByteArrayInputStream(file.getContent().getBytes(Charset.forName("utf-8")));
    }

    protected ApiFilesResponse load() throws IOException {
        final ObjectMapper mapper = createMapper();
        final InputStream files = super.fetchResource("files");
        //TODO when empty, files is an empty array, not object!?
        return mapper.readValue(files, responseClass);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private ApiFile findFile(String name) {
        for (ApiFile file : response.getFiles()) {
            if (name.equals(file.getName()) || name.equals(file.getPath())) {
                return file;
            }
        }
        return null;
    }

}
