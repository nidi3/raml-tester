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
    private final JsonSchemaFactory schemaFactory;
    private final JsonSchemaValidatorSettings schemaValidatorSettings;

    private RestassuredSchemaValidator(JsonSchemaFactory schemaFactory, JsonSchemaValidatorSettings schemaValidatorSettings) {
        this.schemaFactory = schemaFactory;
        this.schemaValidatorSettings = schemaValidatorSettings;
    }

    public RestassuredSchemaValidator() {
        this(null, null);
    }

    public RestassuredSchemaValidator using(JsonSchemaFactory jsonSchemaFactory) {
        return new RestassuredSchemaValidator(jsonSchemaFactory, null);
    }

    public RestassuredSchemaValidator using(JsonSchemaValidatorSettings jsonSchemaValidatorSettings) {
        return new RestassuredSchemaValidator(null, jsonSchemaValidatorSettings);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(MediaType.JSON);
    }

    @Override
    public SchemaValidator withResourceLoader(RamlLoader resourceLoader) {
        final LoadingConfigurationBuilder loadingConfig = LoadingConfiguration.newBuilder();
        final String simpleName = resourceLoader.getClass().getSimpleName();
        loadingConfig.addScheme(simpleName, new RamlLoaderUriDownloader(resourceLoader));
        loadingConfig.setURITranslatorConfiguration(URITranslatorConfiguration.newBuilder().setNamespace(simpleName + ":///").freeze());
        return using(JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfig.freeze()).freeze());
    }

    @SuppressWarnings("unchecked")
    private Matcher<String> getMatcher(String data) {
        if (schemaFactory != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(schemaFactory);
        }
        if (schemaValidatorSettings != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(schemaValidatorSettings);
        }
        return matchesJsonSchema(data);
    }

    @Override
    public void validate(String content, String schema, RamlViolations violations, Message message) {
        try {
            final Matcher<String> matcher = getMatcher(schema);
            if (!matcher.matches(content)) {
                Description description = new StringDescription().appendDescriptionOf(matcher);
                violations.add(message.withParam(description.toString()));
            }
        } catch (JsonSchemaValidationException e) {
            violations.add(message.withMessageParam("restassuredSchemaValidator.schema.invalid", e.getMessage()));
        }
    }
}
