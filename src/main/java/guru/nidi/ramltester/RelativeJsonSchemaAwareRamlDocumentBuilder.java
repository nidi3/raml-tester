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
package guru.nidi.ramltester;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.loader.Loader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.tagresolver.IncludeResolver;
import org.raml.parser.tagresolver.TagResolver;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.TupleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.IOException;
import java.util.Map;

/**
 * Allows !includes of json schemas which reference relative files.
 * By setting the id property accordingly.
 */
class RelativeJsonSchemaAwareRamlDocumentBuilder extends RamlDocumentBuilder {
    private static final Logger log = LoggerFactory.getLogger(RelativeJsonSchemaAwareRamlDocumentBuilder.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final String protocol;
    private NodeTuple schemaTuple;

    public RelativeJsonSchemaAwareRamlDocumentBuilder(Loader loader, ResourceLoader resourceLoader, TagResolver... tagResolvers) {
        super(resourceLoader, tagResolvers);
        //this must match with JsonSchemaFactory.loadingConfiguration
        //see guru.nidi.ramltester.validator.JsonSchemaValidator
        protocol = loader.getClass().getSimpleName();
    }

    @Override
    public boolean onTupleStart(NodeTuple nodeTuple) {
        final Node keyNode = nodeTuple.getKeyNode();
        if (keyNode instanceof ScalarNode) {
            final String name = ((ScalarNode) keyNode).getValue();
            if ("schema".equals(name) || "schemas".equals(name)) {
                if (schemaTuple == null) {
                    schemaTuple = nodeTuple;
                } else {
                    log.warn("Internal error. Nested schema nodes.");
                }
            }
        }
        return super.onTupleStart(nodeTuple);
    }

    @Override
    public void onTupleEnd(NodeTuple nodeTuple) {
        if (nodeTuple == schemaTuple) {
            schemaTuple = null;
        }
        super.onTupleEnd(nodeTuple);
    }

    @Override
    public void onScalar(ScalarNode node, TupleType tupleType) {
        if (schemaTuple != null && node instanceof IncludeResolver.IncludeScalarNode) {
            final String includeName = ((IncludeResolver.IncludeScalarNode) node).getIncludeName();
            if (includeName.endsWith(".json")) {
                try {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> json = mapper.readValue(node.getValue(), Map.class);
                    if (json.containsKey("$schema") && !json.containsKey("id")) {
                        json.put("id", protocol + ":/" + includeName);
                        super.onScalar(new ScalarNode(node.getTag(), node.isResolved(), mapper.writeValueAsString(json), node.getStartMark(), node.getEndMark(), node.getStyle()), tupleType);
                        return;
                    }
                } catch (IOException e) {
                    log.warn("Line {}: Could not parse json file '{}' as schema. Relative $refs inside might not work: {}",
                            node.getStartMark().getLine() + 1, includeName, e.getMessage());
                }
            }
        }
        super.onScalar(node, tupleType);
    }
}
