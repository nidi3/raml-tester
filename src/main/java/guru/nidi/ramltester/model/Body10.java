package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Body10 implements UnifiedBody {
    private TypeDeclaration type;

    public Body10(TypeDeclaration type) {
        this.type = type;
    }

    static List<UnifiedBody> of(List<TypeDeclaration> bodies) {
        final List<UnifiedBody> res = new ArrayList<>();
        for (final TypeDeclaration t : bodies) {
            res.add(new Body10(t));
        }
        return res;
    }

    @Override
    public String name() {
        return type.name();
    }

    @Override
    public List<UnifiedType> formParameters() {
        return null;
    }

    @Override
    public String type() {
        return type.type();
    }

    @Override
    public List<String> examples() {
        final List<String> res = new ArrayList<>();
        for (final ExampleSpec ex : type.examples()) {
            res.add(ex.value());
        }
        return res;
    }
}
