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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class JsonSchemaMatcher extends TypeSafeMatcher<Reader> {
    private final JsonNode schema;
    private final JsonSchemaFactory factory;

    private ProcessingReport report;

    public JsonSchemaMatcher(Reader schema, JsonSchemaFactory factory) {
        try {
            this.schema = JsonLoader.fromReader(schema);
            this.factory = factory != null ? factory : JsonSchemaFactory.byDefault();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean matchesSafely(Reader content) {
        try {
            final JsonSchema jsonSchema = factory.getJsonSchema(schema);
            report = jsonSchema.validate(JsonLoader.fromReader(content));
            return report.isSuccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void describeTo(Description description) {
        if (report != null) {
            description.appendText("The content to match the given JSON schema.\n");
            List<ProcessingMessage> messages = Lists.newArrayList(report);
            if (!messages.isEmpty()) {
                for (final ProcessingMessage message : messages) {
                    description.appendText(message.toString());
                }
            }
        }
    }
}

