package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlDefinition;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 *
 */
public class RamlTestRequestInterceptor implements ClientHttpRequestInterceptor {
    private final RamlDefinition ramlDefinition;

    public RamlTestRequestInterceptor(RamlDefinition ramlDefinition) {
        this.ramlDefinition = ramlDefinition;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        final ClientHttpResponse response = execution.execute(request, body);
        final SpringClientHttpResponseRamlResponse ramlResponse = new SpringClientHttpResponseRamlResponse(response);
        final SpringHttpRequestRamlRequest ramlRequest = new SpringHttpRequestRamlRequest(request, body);
        ramlDefinition.testAgainst(ramlRequest, ramlResponse);
        return ramlResponse;
    }
}
