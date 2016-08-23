package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.resources.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Resource08 implements UnifiedResource {
    private Resource resource;

    public Resource08(Resource resource) {
        this.resource = resource;
    }

    static List<UnifiedResource> of(List<Resource> resources) {
        final List<UnifiedResource> res = new ArrayList<>();
        for (final Resource r : resources) {
            res.add(new Resource08(r));
        }
        return res;
    }

    @Override
    public String description() {
        return resource.description().value();
    }

    @Override
    public String displayName() {
        return resource.displayName();
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
        return new Resource08(resource.parentResource());
    }

    @Override
    public List<UnifiedMethod> methods() {
        return Method08.of(resource.methods());
    }

    @Override
    public List<UnifiedType> uriParameters() {
        return Type08.of(resource.uriParameters());
    }

    @Override
    public List<UnifiedType> baseUriParameters() {
        return Type08.of(resource.baseUriParameters());
    }

    @Override
    public List<UnifiedSecSchemeRef> securedBy() {
        return SecSchemeRef08.of(resource.securedBy());
    }
}
