package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Type08 implements UnifiedType {
    private Parameter parameter;

    public Type08(Parameter parameter) {
        this.parameter = parameter;
    }

    static List<UnifiedType> of(List<Parameter> parameters) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final Parameter p : parameters) {
            res.add(new Type08(p));
        }
        return res;
    }

    @Override
    public String name() {
        return parameter.name();
    }

    @Override
    public String description() {
        return parameter.description().value();
    }

}
