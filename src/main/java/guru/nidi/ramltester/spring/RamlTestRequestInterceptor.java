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
    private final RamlRestTemplate restTemplate;
    private final RamlDefinition ramlDefinition;
    private final String baseUri;

    public RamlTestRequestInterceptor(RamlRestTemplate restTemplate, RamlDefinition ramlDefinition, String baseUri) {
        this.restTemplate = restTemplate;
        this.ramlDefinition = ramlDefinition;
        this.baseUri = baseUri;
    }

    public RamlDefinition getRamlDefinition() {
        return ramlDefinition;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        restTemplate.setRamlReport(null);
        final ClientHttpResponse response = execution.execute(request, body);
        final SpringClientHttpResponseRamlResponse ramlResponse = new SpringClientHttpResponseRamlResponse(response);
        final SpringHttpRequestRamlRequest ramlRequest = new SpringHttpRequestRamlRequest(request, baseUri, body);
        restTemplate.setRamlReport(ramlDefinition.testAgainst(ramlRequest, ramlResponse));
        return ramlResponse;
    }
}
