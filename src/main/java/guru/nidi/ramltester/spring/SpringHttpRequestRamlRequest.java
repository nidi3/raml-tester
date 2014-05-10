package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlRequest;
import guru.nidi.ramltester.util.UriComponents;
import org.springframework.http.HttpRequest;

import java.util.Map;

/**
 *
 */
public class SpringHttpRequestRamlRequest implements RamlRequest {
    private final HttpRequest request;
    private final String baseUri;
    private final byte[] body;
    private final UriComponents uriComponents;

    public SpringHttpRequestRamlRequest(HttpRequest request, String baseUri, byte[] body) {
        this.request = request;
        this.baseUri = baseUri;
        this.body = body;
        this.uriComponents = UriComponents.fromHttpUrl(request.getURI().toString());
    }

    @Override
    public String getRequestUrl() {
        return (baseUri != null ? baseUri : uriComponents.getServer()) + uriComponents.getPath();
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return uriComponents.getQueryParameters().getValues();
    }
}
