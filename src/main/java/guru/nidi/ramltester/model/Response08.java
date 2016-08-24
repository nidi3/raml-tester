package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.bodies.Response;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Response08 implements UnifiedResponse {
    private Response response;

    public Response08(Response response) {
        this.response = response;
    }

    static List<UnifiedResponse> of(List<Response> responses) {
        final List<UnifiedResponse> res = new ArrayList<>();
        for (final Response r : responses) {
            res.add(new Response08(r));
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
        return Type08.of(response.headers());
    }

    @Override
    public List<UnifiedBody> body() {
        return Body08.of(response.body());
    }
}
