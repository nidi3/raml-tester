package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.api.Api;

import java.util.List;

/**
 *
 */
public class Api08 implements UnifiedApi {
    private Api api;

    public Api08(Api api) {
        this.api = api;
    }

    @Override
    public String title() {
        return api.title();
    }

    @Override
    public String version() {
        return api.version();
    }

    @Override
    public String baseUri() {
        return api.baseUri().value();
    }

    @Override
    public List<String> protocols() {
        return api.protocols();
    }

    @Override
    public String ramlVersion() {
        return api.ramlVersion();
    }

    @Override
    public List<UnifiedResource> resources() {
        return Resource08.of(api.resources());
    }

    @Override
    public List<UnifiedType> baseUriParameters() {
        return Type08.of(api.baseUriParameters());
    }

    @Override
    public List<UnifiedDocItem> documentation() {
        return DocItem08.of(api.documentation());
    }

    @Override
    public List<UnifiedSecScheme> securitySchemes() {
        return SecScheme08.of(api.securitySchemes());
    }

    @Override
    public List<UnifiedSecSchemeRef> securedBy() {
        return SecSchemeRef08.of(api.securedBy());
    }

}
