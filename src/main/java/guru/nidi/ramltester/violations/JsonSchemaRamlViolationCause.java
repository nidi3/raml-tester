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
package guru.nidi.ramltester.violations;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import guru.nidi.ramltester.model.RamlViolationCause;

/**
 * Created by arielsegura on 10/9/16.
 */
public class JsonSchemaRamlViolationCause implements RamlViolationCause {

    String message;
    String asJson;

    public JsonSchemaRamlViolationCause(ProcessingMessage processingMessage) {
        message = processingMessage.getMessage();
        asJson = processingMessage.asJson().toString();
    }

    public JsonSchemaRamlViolationCause(ProcessingException e) {
        message = e.getShortMessage();
        asJson = e.getProcessingMessage().asJson().toString();
    }

    public JsonSchemaRamlViolationCause(JsonParseException e) throws JsonProcessingException {
        message = e.getMessage();
        asJson = RamlViolationCause.super.asJson();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String asJson() {
        return asJson;
    }
}
