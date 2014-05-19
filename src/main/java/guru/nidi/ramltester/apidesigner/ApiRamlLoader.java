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

import guru.nidi.ramltester.loader.FormLoginUrlFetcher;
import guru.nidi.ramltester.loader.RepositoryRamlLoader;
import guru.nidi.ramltester.loader.SimpleUrlFetcher;
import guru.nidi.ramltester.loader.UrlRamlLoader;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class ApiRamlLoader extends RepositoryRamlLoader {
    public ApiRamlLoader(String user, String password) throws IOException {
        super(new UrlRamlLoader("http://api-portal.anypoint.mulesoft.com",
                new FormLoginUrlFetcher("rest/raml/v1", "ajax/apihub/login-register/form?section=login", user, password, "name", "pass") {
                    @Override
                    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
                        parameters.add(new BasicNameValuePair("form_id", "user_login"));
                    }
                }
        ), "files", ApiPortalFilesResponse.class);
    }

    public ApiRamlLoader(String baseUrl) throws IOException {
        super(new UrlRamlLoader(baseUrl, new SimpleUrlFetcher()), "files", ApiDesignerFilesResponse.class);
    }
}
