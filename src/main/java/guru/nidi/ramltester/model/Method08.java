package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.methods.Method;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Method08 implements UnifiedMethod {
    private Method method;

    public Method08(Method method) {
        this.method = method;
    }

    static List<UnifiedMethod> of(List<Method> methods) {
        final List<UnifiedMethod> res = new ArrayList<>();
        for (final Method m : methods) {
            res.add(new Method08(m));
        }
        return res;
    }

    @Override
    public String method() {
        return method.method();
    }

    @Override
    public UnifiedResource resource() {
        return new Resource08(method.resource());
    }

    @Override
    public List<String> protocols() {
        return method.protocols();
    }

    @Override
    public List<UnifiedType> queryParameters() {
        return Type08.of(method.queryParameters());
    }

    @Override
    public List<UnifiedType> headers() {
        return Type08.of(method.headers());
    }

    @Override
    public String description() {
        return method.description().value();
    }

    @Override
    public List<UnifiedType> baseUriParameters() {
        return Type08.of(method.baseUriParameters());
    }

    @Override
    public List<UnifiedResponse> responses() {
        return Response08.of(method.responses());
    }

    @Override
    public List<UnifiedSecSchemeRef> securedBy() {
        return SecSchemeRef08.of(method.securedBy());
    }
}
