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

import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.*;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class RamlValidator {
    private final Raml raml;
    private final List<SchemaValidator> schemaValidators;
    private RamlViolations violations;

    public RamlValidator(Raml raml, List<SchemaValidator> schemaValidators) {
        this.raml = raml;
        this.schemaValidators = schemaValidators;
    }

    public RamlReport validate() {
        final RamlReport report = new RamlReport(raml);
        violations = report.getValidationViolations();
        for (Resource resource : raml.getResources().values()) {
            checkResource(resource);
        }
        return report;
    }

    private void checkResource(Resource resource) {
        for (Resource res : resource.getResources().values()) {
            checkResource(res);
        }
        for (Action action : resource.getActions().values()) {
            checkAction(action);
        }
    }

    private void checkAction(Action action) {
        if (action.getBody() != null) {
            for (MimeType mimeType : action.getBody().values()) {
                checkMimeType(action, mimeType, "");
            }
        }
        for (Map.Entry<String, Response> entry : action.getResponses().entrySet()) {
            checkResponse(action, entry.getKey(), entry.getValue());
        }
    }

    private void checkResponse(Action action, String code, Response response) {
        if (response.getBody() != null) {
            final String detail = new Message("response", code).toString();
            for (MimeType mimeType : response.getBody().values()) {
                checkMimeType(action, mimeType, detail);
            }
        }
    }

    private void checkMimeType(Action action, MimeType mimeType, String detail) {
        final SchemaValidator validator = CheckerHelper.findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.getType()));
        if (mimeType.getExample() != null && validator != null) {
            final String schema = mimeType.getSchema();
            final String refSchema = raml.getConsolidatedSchemas().get(schema);
            validator.validate(mimeType.getExample(), refSchema != null ? refSchema : schema, violations,
                    new Message("schema.example.mismatch", action, detail, mimeType, mimeType.getExample()));
        }
    }
}
