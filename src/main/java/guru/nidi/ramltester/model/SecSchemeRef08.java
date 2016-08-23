package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.security.SecuritySchemeRef;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SecSchemeRef08 implements UnifiedSecSchemeRef {
    private SecuritySchemeRef ref;

    public SecSchemeRef08(SecuritySchemeRef ref) {
        this.ref = ref;
    }
    static List<UnifiedSecSchemeRef> of(List<SecuritySchemeRef> refs) {
        final List<UnifiedSecSchemeRef> res = new ArrayList<>();
        for (final SecuritySchemeRef r : refs) {
            res.add(new SecSchemeRef08(r));
        }
        return res;
    }
    @Override
    public UnifiedSecScheme securityScheme() {
        return new SecScheme08(ref.securityScheme());
    }
}
