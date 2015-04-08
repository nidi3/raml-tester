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

import guru.nidi.ramltester.loader.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 *
 */
public class ApiRamlLoader extends RepositoryRamlLoader {
    public ApiRamlLoader(String user, String password) {
        this(null, user, password);
    }

    public ApiRamlLoader(String base, String user, String password) {
        super(new UrlRamlLoader("http://api-portal.anypoint.mulesoft.com",
                new FormLoginUrlFetcher("rest/raml/v1", "ajax/apihub/login-register/form?section=login", user, password, "name", "pass") {
                    @Override
                    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
                        parameters.add(new BasicNameValuePair("form_id", "user_login"));
                    }
                }
        ), base, "files", ApiPortalFilesResponse.class);
    }

    public ApiRamlLoader(String baseUrl) {
        super(new UrlRamlLoader(baseUrl, new SimpleUrlFetcher()), null, "files", ApiDesignerFilesResponse.class);
    }

    public static class PortalFactory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "apiportal";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            return new ApiRamlLoader(base, username, password);
        }
    }

    public static class DesignerFactory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "apidesigner";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            return new ApiRamlLoader(base);
        }
    }
}
