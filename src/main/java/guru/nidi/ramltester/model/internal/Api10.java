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
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v10.api.Api;

import java.util.List;

public class Api10 implements RamlApi {
    private final Api api;

    public Api10(Api api) {
        this.api = api;
    }

    @Override
    public String title() {
        return api.title().value();
    }

    @Override
    public String version() {
        return api.version().value();
    }

    @Override
    public String baseUri() {
        return api.baseUri() == null ? null : api.baseUri().value();
    }

    @Override
    public List<String> protocols() {
        return api.protocols();
    }

    @Override
    public String ramlVersion() {
        return api.ramlVersion();
    }

    @Override
    public List<RamlResource> resources() {
        return Resource10.of(api.resources());
    }

    @Override
    public List<RamlType> baseUriParameters() {
        return Type10.of(api.baseUriParameters());
    }

    @Override
    public List<RamlDocItem> documentation() {
        return DocItem10.of(api.documentation());
    }

    @Override
    public String description() {
        return api.description() == null ? null : api.description().value();
    }

    @Override
    public List<RamlSecScheme> securitySchemes() {
        return SecScheme10.of(api.securitySchemes());
    }

    @Override
    public List<RamlSecSchemeRef> securedBy() {
        return SecSchemeRef10.of(api.securedBy());
    }

}
