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

import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.module.jsv.JsonSchemaValidationException;
import com.jayway.restassured.module.jsv.JsonSchemaValidatorSettings;
import guru.nidi.ramltester.loader.RamlLoader;
import guru.nidi.ramltester.loader.RamlLoaderUriDownloader;
import guru.nidi.ramltester.util.MediaType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

/**
 *
 */
public class RestassuredSchemaValidator implements SchemaValidator {
    private JsonSchemaFactory factory;
    private final JsonSchemaValidatorSettings settings;
    private final RamlLoader loader;

    private RestassuredSchemaValidator(JsonSchemaFactory factory, JsonSchemaValidatorSettings settings, RamlLoader loader) {
        this.factory = factory;
        this.settings = settings;
        this.loader = loader;
    }

    public RestassuredSchemaValidator() {
        this(null, null, null);
    }

    public RestassuredSchemaValidator using(JsonSchemaFactory factory) {
        return new RestassuredSchemaValidator(factory, settings, loader);
    }

    public RestassuredSchemaValidator using(JsonSchemaValidatorSettings settings) {
        return new RestassuredSchemaValidator(factory, settings, loader);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(MediaType.JSON);
    }

    @Override
    public SchemaValidator withResourceLoader(RamlLoader loader) {
        return new RestassuredSchemaValidator(factory, settings, loader);
    }

    private synchronized void init() {
        if (loader != null && factory == null) {
            final LoadingConfigurationBuilder loadingConfig = LoadingConfiguration.newBuilder();
            final String simpleName = loader.getClass().getSimpleName();
            loadingConfig.addScheme(simpleName, new RamlLoaderUriDownloader(loader));
            loadingConfig.setURITranslatorConfiguration(URITranslatorConfiguration.newBuilder().setNamespace(simpleName + ":///").freeze());
            factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfig.freeze()).freeze();
        }
    }

    @SuppressWarnings("unchecked")
    private Matcher<String> getMatcher(String data) {
        if (factory != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(factory);
        }
        if (settings != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(settings);
        }
        return matchesJsonSchema(data);
    }

    @Override
    public void validate(String content, String schema, RamlViolations violations, Message message) {
        init();
        try {
            final Matcher<String> matcher = getMatcher(schema);
            if (!matcher.matches(content)) {
                final Description description = new StringDescription().appendDescriptionOf(matcher);
                violations.add(message.withParam(description.toString()));
            }
        } catch (JsonSchemaValidationException e) {
            violations.add(message.withMessageParam("restassuredSchemaValidator.schema.invalid", e.getMessage()));
        }
    }
}
