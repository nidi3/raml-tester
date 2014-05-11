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
import guru.nidi.ramltester.loader.RamlResourceLoader;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ApiPortalLoader implements RamlResourceLoader {
    private final String user;
    private final String password;
    private final ApiPortalFilesResponse response;

    public ApiPortalLoader(String user, String password) throws IOException {
        this.user = user;
        this.password = password;
        this.response = load();
    }

    @Override
    public InputStream fetchResource(String resourceName) {
        final ApiPortalFile file = findFile(resourceName);
        return file != null
                ? new ByteArrayInputStream(file.getContent().getBytes(Charset.forName("utf-8")))
                : null;
    }

    private ApiPortalFilesResponse load() throws IOException {
        final ObjectMapper mapper = createMapper();
        try (final CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final HttpPost login = new HttpPost("http://api-portal.anypoint.mulesoft.com/ajax/apihub/login-register/form?section=login");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("name", user));
            params.add(new BasicNameValuePair("pass", password));
            params.add(new BasicNameValuePair("form_id", "user_login"));
            login.setEntity(new UrlEncodedFormEntity(params));
            final CloseableHttpResponse loginResponse = client.execute(login);
            if (loginResponse.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new IOException("Login into api portal not successful: " + loginResponse.getStatusLine().getReasonPhrase());
            }
            final HttpGet files = new HttpGet("http://api-portal.anypoint.mulesoft.com/rest/raml/v1/files");
            final CloseableHttpResponse filesResponse = client.execute(files);
            if (filesResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Could not get list of files: " + filesResponse.getStatusLine().getReasonPhrase());
            }
            //final Map map = createMapper.readValue(filesResponse.getEntity().getContent(), Map.class);
            return mapper.readValue(filesResponse.getEntity().getContent(), ApiPortalFilesResponse.class);
        }
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
