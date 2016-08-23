package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.security.SecuritySchemePart;

import java.util.List;

/**
 *
 */
public class SecSchemePart10 implements UnifiedSecSchemePart{
    private SecuritySchemePart part;

    public SecSchemePart10(SecuritySchemePart part) {
        this.part = part;
    }

    @Override
    public List<UnifiedResponse> responses() {
        return Response10.of(part.responses());
    }

    @Override
    public List<UnifiedType> queryParameters() {
        return Type10.of(part.queryParameters());
    }

    @Override
    public List<UnifiedType> headers() {
        return Type10.of(part.headers());
    }
}
