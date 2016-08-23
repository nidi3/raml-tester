package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.api.Api;

import java.util.List;

/**
 *
 */
public class Api10 implements UnifiedApi {
    private Api api;

    public Api10(Api api) {
        this.api = api;
    }

    @Override
    public String title() {
        return api.title().value();
    }

    @Override
    public String version() {
        return api.version().value();
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
        return Resource10.of(api.resources());
    }

    @Override
    public List<UnifiedType> baseUriParameters() {
        return Type10.of(api.baseUriParameters());
    }

    @Override
    public List<UnifiedDocItem> documentation() {
        return DocItem10.of(api.documentation());
    }

    @Override
    public List<UnifiedSecScheme> securitySchemes() {
        return SecScheme10.of(api.securitySchemes());
    }

    @Override
    public List<UnifiedSecSchemeRef> securedBy() {
        return SecSchemeRef10.of(api.securedBy());
    }

}
