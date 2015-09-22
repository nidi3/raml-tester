package guru.nidi.ramltester;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.loader.Loader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.tagresolver.IncludeResolver;
import org.raml.parser.tagresolver.TagResolver;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.TupleType;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
class RelativeJsonSchemaRamlDocBuilder extends RamlDocumentBuilder {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String protocol;

    public RelativeJsonSchemaRamlDocBuilder(Loader loader, ResourceLoader resourceLoader, TagResolver... tagResolvers) {
        super(resourceLoader, tagResolvers);
        //this must match with JsonSchemaFactory.loadingConfiguration, see guru.nidi.ramltester.validator.JsonSchemaValidator
        protocol = loader.getClass().getSimpleName();
    }

    @Override
    public void onScalar(ScalarNode node, TupleType tupleType) {
        if (node instanceof IncludeResolver.IncludeScalarNode) {
            final String includeName = ((IncludeResolver.IncludeScalarNode) node).getIncludeName();
            if (includeName.endsWith(".json")) {
                try {
                    final Map<String, Object> json = mapper.readValue(node.getValue(), Map.class);
                    if (json.containsKey("$schema") && !json.containsKey("id")) {
                        json.put("id", protocol + ":/" + includeName);
                        super.onScalar(new ScalarNode(node.getTag(), node.isResolved(), mapper.writeValueAsString(json), node.getStartMark(), node.getEndMark(), node.getStyle()), tupleType);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onScalar(node, tupleType);
    }
}
