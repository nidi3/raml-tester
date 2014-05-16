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
package guru.nidi.ramltester.apiportal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramltester.loader.FormLoginUrlRamlResourceLoader;
import guru.nidi.ramltester.loader.RamlResourceLoader;
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
public class ApiPortalLoader extends FormLoginUrlRamlResourceLoader implements RamlResourceLoader {
    private final ApiPortalFilesResponse response;

    public ApiPortalLoader(String user, String password) throws IOException {
        super("http://api-portal.anypoint.mulesoft.com", "rest/raml/v1", "ajax/apihub/login-register/form?section=login",
                user, password, "name", "pass");
        this.response = load();
    }

    @Override
    public InputStream fetchResource(String resourceName) {
        final ApiPortalFile file = findFile(resourceName);
        if (file == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        return new ByteArrayInputStream(file.getContent().getBytes(Charset.forName("utf-8")));
    }

    @Override
    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
        parameters.add(new BasicNameValuePair("form_id", "user_login"));
    }

    private ApiPortalFilesResponse load() throws IOException {
        final ObjectMapper mapper = createMapper();
        final InputStream files = super.fetchResource("files");
        return mapper.readValue(files, ApiPortalFilesResponse.class);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private ApiPortalFile findFile(String name) {
        for (ApiPortalFile file : response.getFiles().values()) {
            if (name.equals(file.getName()) || name.equals(file.getPath())) {
                return file;
            }
        }
        return null;
    }

}
