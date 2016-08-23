package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.security.SecuritySchemeRef;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SecSchemeRef10 implements UnifiedSecSchemeRef {
    private SecuritySchemeRef ref;

    public SecSchemeRef10(SecuritySchemeRef ref) {
        this.ref = ref;
    }
    static List<UnifiedSecSchemeRef> of(List<SecuritySchemeRef> refs) {
        final List<UnifiedSecSchemeRef> res = new ArrayList<>();
        for (final SecuritySchemeRef r : refs) {
            res.add(new SecSchemeRef10(r));
        }
        return res;
    }

    @Override
    public UnifiedSecScheme securityScheme() {
        return new SecScheme10(ref.securityScheme());
    }
}
