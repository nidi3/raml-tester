package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.security.SecuritySchemePart;

import java.util.List;

/**
 *
 */
public class SecSchemePart08 implements UnifiedSecSchemePart{
    private SecuritySchemePart part;

    public SecSchemePart08(SecuritySchemePart part) {
        this.part = part;
    }

    @Override
    public List<UnifiedResponse> responses() {
        return Response08.of(part.responses());
    }

    @Override
    public List<UnifiedType> queryParameters() {
        return Type08.of(part.queryParameters());
    }

    @Override
    public List<UnifiedType> headers() {
        return Type08.of(part.headers());
    }
}
