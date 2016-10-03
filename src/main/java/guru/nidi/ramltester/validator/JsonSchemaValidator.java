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
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.report.ReportProvider;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import guru.nidi.loader.Loader;
import guru.nidi.loader.use.jsonschema.LoaderUriDownloader;
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
    private final LoadingConfiguration loadingConfiguration;
    private final ReportProvider reportProvider;
    private final ValidationConfiguration validationConfiguration;
    private final Loader loader;

    private JsonSchemaValidator(Loader loader, LoadingConfiguration loadingConfiguration, ReportProvider reportProvider, ValidationConfiguration validationConfiguration) {
        this.loader = loader;
        this.loadingConfiguration = loadingConfiguration;
        this.reportProvider = reportProvider;
        this.validationConfiguration = validationConfiguration;
    }

    public JsonSchemaValidator() {
        this(null, null, null, null);
    }

    public JsonSchemaValidator using(LoadingConfiguration loadingConfiguration) {
        return new JsonSchemaValidator(loader, loadingConfiguration, reportProvider, validationConfiguration);
    }

    public JsonSchemaValidator using(ReportProvider reportProvider) {
        return new JsonSchemaValidator(loader, loadingConfiguration, reportProvider, validationConfiguration);
    }

    public JsonSchemaValidator using(ValidationConfiguration validationConfiguration) {
        return new JsonSchemaValidator(loader, loadingConfiguration, reportProvider, validationConfiguration);
    }

    @Override
    public SchemaValidator withLoader(Loader loader) {
        return new JsonSchemaValidator(loader, loadingConfiguration, reportProvider, validationConfiguration);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(MediaType.JSON);
    }

    private synchronized void init() {
        if (factory == null) {
            factory = loader == null
                    ? JsonSchemaFactory.byDefault()
                    : LoaderUriDownloader.createJsonSchemaFactory(loader, loadingConfiguration, reportProvider, validationConfiguration);
        }
    }

    @Override
    public void validate(Reader content, Reader schema, RamlViolations violations, Message message) {
        init();
        try (final Reader s = schema) {
            final JsonSchema jsonSchema = factory.getJsonSchema(JsonLoader.fromReader(schema));
            final ProcessingReport report = jsonSchema.validate(JsonLoader.fromReader(content));
            if (!report.isSuccess()) {
                String msg = "";
                for (final ProcessingMessage reportLine : report) {
                    msg += reportLine.toString() + "\n";
                }
                violations.add(message.withParam(msg), report);
            }
        } catch (ProcessingException | IOException e) {
            violations.add(message.withMessageParam("jsonSchemaValidator.schema.invalid", e.getMessage()));
        }
    }
}
