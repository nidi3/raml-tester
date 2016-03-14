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
import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.processing.Processor;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.jsonschema.keyword.validator.AbstractKeywordValidator;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;

import java.util.EnumSet;

/**
 * Keyword validator for draft v4's {@code type}
 */
public final class MyTypeValidator extends AbstractKeywordValidator {
    private final EnumSet<NodeType> types = EnumSet.noneOf(NodeType.class);

    public MyTypeValidator(final JsonNode digest) {
        super("type");
        for (final JsonNode node : digest.get(keyword)) {
            types.add(NodeType.fromName(node.textValue()));
        }
    }

    @Override
    public void validate(final Processor<FullData, FullData> processor, final ProcessingReport report, final MessageBundle bundle, final FullData data) throws ProcessingException {
        final NodeType type = NodeType.getNodeType(data.getInstance().getNode());
        final SchemaTree schema = data.getSchema();
        System.out.println(schema);
        System.out.println(schema.setPointer(schema.getPointer().parent()));
//        schema.setPointer(schema.getPointer().parent().parent()).getNode().findPath("required")
        if (!types.contains(type)) {
            report.error(newMsg(data, bundle, "err.common.typeNoMatch")
                    .putArgument("found", type)
                    .putArgument("expected", toArrayNode(types)));
        }
    }

    @Override
    public String toString() {
        return keyword + ": " + types;
    }
}
