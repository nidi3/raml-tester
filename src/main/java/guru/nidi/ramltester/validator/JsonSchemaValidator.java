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
package guru.nidi.ramltester.validator;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import guru.nidi.loader.Loader;
import guru.nidi.loader.use.jsonschema.LoaderUriDownloader;
import guru.nidi.ramltester.core.JsonSchemaViolationCause;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;

import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class JsonSchemaValidator implements SchemaValidator {
    private JsonSchemaFactory factory;
    private final Loader loader;

    private JsonSchemaValidator(JsonSchemaFactory factory, Loader loader) {
        this.factory = factory;
        this.loader = loader;
    }

    public JsonSchemaValidator() {
        this(null, null);
    }

    public JsonSchemaValidator using(JsonSchemaFactory factory) {
        return new JsonSchemaValidator(factory, loader);
    }

    @Override
    public SchemaValidator withLoader(Loader loader) {
        return new JsonSchemaValidator(factory, loader);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(MediaType.JSON);
    }

    private synchronized void init() {
        if (loader != null && factory == null) {
            final LoadingConfigurationBuilder loadingConfig = LoadingConfiguration.newBuilder();
            final String simpleName = loader.getClass().getSimpleName();
            loadingConfig.addScheme(simpleName, new LoaderUriDownloader(loader));
            loadingConfig.setURITranslatorConfiguration(URITranslatorConfiguration.newBuilder().setNamespace(simpleName + ":///").freeze());
            factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfig.freeze()).freeze();
        }
    }

    @Override
    public void validate(Reader content, Reader schema, RamlViolations violations, Message message) {
        init();
        try (final Reader s = schema) {
            final JsonSchemaFactory factory = this.factory == null ? JsonSchemaFactory.byDefault() : this.factory;
            final JsonSchema jsonSchema = factory.getJsonSchema(JsonLoader.fromReader(schema));
            final ProcessingReport report = jsonSchema.validate(JsonLoader.fromReader(content));
            if (!report.isSuccess()) {
                String msg = "";
                for (final ProcessingMessage reportLine : report) {
                    msg += new Message("jsonSchemaValidator.message", reportLine.toString());
                }
                violations.add(message.withParam(msg), new JsonSchemaViolationCause(report));
            }
        } catch (ProcessingException e) {
            violations.add(message.withMessageParam("schema.invalid", e.getMessage()), new JsonSchemaViolationCause(e));
        } catch (IOException e) {
            violations.add(message.withMessageParam("schema.invalid", e.getMessage()));
        }
    }
}
