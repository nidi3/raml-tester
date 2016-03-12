/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
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
public final class MyTypeValidator
    extends AbstractKeywordValidator
{
    private final EnumSet<NodeType> types = EnumSet.noneOf(NodeType.class);

    public MyTypeValidator(final JsonNode digest)
    {
        super("type");
        for (final JsonNode node: digest.get(keyword))
            types.add(NodeType.fromName(node.textValue()));
    }

    @Override
    public void validate(final Processor<FullData, FullData> processor,
        final ProcessingReport report, final MessageBundle bundle,
        final FullData data)
        throws ProcessingException
    {
        final NodeType type
            = NodeType.getNodeType(data.getInstance().getNode());
        final SchemaTree schema = data.getSchema();
        System.out.println(schema);
        System.out.println(schema.setPointer(schema.getPointer().parent()));
//        schema.setPointer(schema.getPointer().parent().parent()).getNode().findPath("required")
        if (!types.contains(type))
            report.error(newMsg(data, bundle, "err.common.typeNoMatch")
                .putArgument("found", type)
                .putArgument("expected", toArrayNode(types)));
    }

    @Override
    public String toString()
    {
        return keyword + ": " + types;
    }
}
