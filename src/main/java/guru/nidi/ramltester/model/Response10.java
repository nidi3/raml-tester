package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.bodies.Response;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Response10 implements UnifiedResponse {
    private Response response;

    public Response10(Response response) {
        this.response = response;
    }

    static List<UnifiedResponse> of(List<Response> responses) {
        final List<UnifiedResponse> res = new ArrayList<>();
        for (final Response r : responses) {
            res.add(new Response10(r));
        }
        return res;
    }

    @Override
    public String description() {
        return response.description().value();
    }

    @Override
    public String code() {
        return response.code().value();
    }

    @Override
    public List<UnifiedType> headers() {
        return Type10.of(response.headers());
    }

    @Override
    public List<UnifiedBody> body() {
        return Body10.of(response.body());
    }
}
