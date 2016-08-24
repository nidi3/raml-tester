package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.bodies.BodyLike;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 *
 */
public class Body08 implements UnifiedBody {
    private BodyLike body;

    public Body08(BodyLike body) {
        this.body = body;
    }
    static List<UnifiedBody> of(List<BodyLike> bodies) {
        final List<UnifiedBody> res = new ArrayList<>();
        for (final BodyLike b : bodies) {
            res.add(new Body08(b));
        }
        return res;
    }

    @Override
    public String name() {
        return body.name();
    }

    @Override
    public List<UnifiedType> formParameters() {
        return Type08.of(body.formParameters());
    }

    @Override
    public String type() {
        return body.schema().value(); //TODO body.schemaContent() when it's working
    }

    @Override
    public List<String> examples() {
        return singletonList(body.example().value());
    }
}
