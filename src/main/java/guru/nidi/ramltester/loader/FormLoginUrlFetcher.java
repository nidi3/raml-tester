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
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FormLoginUrlFetcher extends SimpleUrlFetcher {
    private final String loadPath;
    private final String loginUrl;
    private final String login;
    private final String password;
    private final String loginField;
    private final String passwordField;

    public FormLoginUrlFetcher(String loadPath, String loginPath, String login, String password, String loginField, String passwordField) {
        this.loadPath = loadPath;
        this.login = login;
        this.password = password;
        this.loginUrl = loginPath;
        this.loginField = loginField;
        this.passwordField = passwordField;
    }

    @Override
    public InputStream fetchFromUrl(CloseableHttpClient client, String base, String name, long ifModifiedSince) {
        try {
            final HttpPost login = new HttpPost(base + "/" + loginUrl);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(loginField, this.login));
            params.add(new BasicNameValuePair(passwordField, password));
            postProcessLoginParameters(params);
            login.setEntity(new UrlEncodedFormEntity(params));
            final CloseableHttpResponse getResult = client.execute(postProcessLogin(login));
            if (getResult.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new RamlLoader.ResourceNotFoundException(name, "Could not login: " + getResult.getStatusLine().toString());
            }
            EntityUtils.consume(getResult.getEntity());
            return super.fetchFromUrl(client, base + "/" + loadPath, name, ifModifiedSince);
        } catch (IOException e) {
            throw new RamlLoader.ResourceNotFoundException(name, e);
        }
    }

    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
    }

    protected HttpPost postProcessLogin(HttpPost login) {
        return login;
    }
}
