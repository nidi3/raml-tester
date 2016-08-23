package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.resources.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Resource10 implements UnifiedResource {
    private Resource resource;

    public Resource10(Resource resource) {
        this.resource = resource;
    }

    static List<UnifiedResource> of(List<Resource> resources) {
        final List<UnifiedResource> res = new ArrayList<>();
        for (final Resource r : resources) {
            res.add(new Resource10(r));
        }
        return res;
    }

    @Override
    public String description() {
        return resource.description().value();
    }

    @Override
    public String displayName() {
        return resource.displayName().value();
    }

    @Override
    public String relativeUri() {
        return resource.relativeUri().value();
    }

    @Override
    public List<UnifiedResource> resources() {
        return of(resource.resources());
    }

    @Override
    public String resourcePath() {
        return resource.resourcePath();
    }

    @Override
    public UnifiedResource parentResource() {
        return new Resource10(resource.parentResource());
    }

    @Override
    public List<UnifiedMethod> methods() {
        return Method10.of(resource.methods());
    }

    @Override
    public List<UnifiedType> uriParameters() {
        return Type10.of(resource.uriParameters());
    }

    @Override
    public List<UnifiedType> baseUriParameters() {
        return null;
    }

    @Override
    public List<UnifiedSecSchemeRef> securedBy() {
        return SecSchemeRef10.of(resource.securedBy());
    }
}
