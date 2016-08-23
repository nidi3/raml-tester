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
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.Api08;
import guru.nidi.ramltester.model.Api10;
import guru.nidi.ramltester.model.UnifiedApi;
import org.raml.v2.api.RamlModelResult;

import java.util.List;

/**
 *
 */
public class CheckerConfig {
    public final RamlModelResult raml;
    public final List<SchemaValidator> schemaValidators;
    public final String baseUri;
    public final boolean includeServletPath;
    public final boolean ignoreXheaders;
    public final boolean failFast;

    public CheckerConfig(RamlModelResult raml, List<SchemaValidator> schemaValidators) {
        this(raml, schemaValidators, null, false, false, false);
    }

    public CheckerConfig(RamlModelResult raml, List<SchemaValidator> schemaValidators, String baseUri, boolean includeServletPath, boolean ignoreXheaders, boolean failFast) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
        this.baseUri = baseUri;
        this.includeServletPath = includeServletPath;
        this.ignoreXheaders = ignoreXheaders;
        this.failFast = failFast;
    }

    public UnifiedApi getRaml() {
        return raml.isVersion08()
                ? new Api08(raml.getApiV08())
                : new Api10(raml.getApiV10());
    }

    public CheckerConfig assumingBaseUri(String baseUri) {
        return new CheckerConfig(raml, schemaValidators, baseUri, includeServletPath, ignoreXheaders, failFast);
    }

    public CheckerConfig assumingBaseUri(String baseUri, boolean includeServletPath) {
        return new CheckerConfig(raml, schemaValidators, baseUri, includeServletPath, ignoreXheaders, failFast);
    }

    public CheckerConfig ignoringXheaders() {
        return ignoringXheaders(true);
    }

    public CheckerConfig ignoringXheaders(boolean ignoreXheaders) {
        return new CheckerConfig(raml, schemaValidators, baseUri, includeServletPath, ignoreXheaders, failFast);
    }

    public CheckerConfig includeServletPath() {
        return includeServletPath(true);
    }

    public CheckerConfig includeServletPath(boolean includeServletPath) {
        return new CheckerConfig(raml, schemaValidators, baseUri, includeServletPath, ignoreXheaders, failFast);
    }

    public CheckerConfig failFast() {
        return failFast(true);
    }

    public CheckerConfig failFast(boolean failFast) {
        return new CheckerConfig(raml, schemaValidators, baseUri, includeServletPath, ignoreXheaders, failFast);
    }
}
