package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlReport;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 *
 */
public class RamlRestTemplate extends RestTemplate {
    private final ClientHttpRequestFactory originalRequestFactory;
    private final RamlTestRequestInterceptor interceptor;
    private RamlReport lastReport;

    public RamlRestTemplate(RamlDefinition ramlDefinition, String baseUri, ClientHttpRequestFactory requestFactory) {
        originalRequestFactory = requestFactory;
        interceptor = new RamlTestRequestInterceptor(this, ramlDefinition, baseUri);
        setRequestFactory(new InterceptingClientHttpRequestFactory(requestFactory, Collections.<ClientHttpRequestInterceptor>singletonList(interceptor)));
    }

    public RamlRestTemplate(RamlDefinition ramlDefinition, String baseUri, RestTemplate restTemplate) {
        this(ramlDefinition, baseUri, restTemplate.getRequestFactory());
        init(restTemplate);
    }

    public RamlRestTemplate(RamlDefinition ramlDefinition, String baseUri, RamlRestTemplate restTemplate) {
        this(ramlDefinition, baseUri, restTemplate.originalRequestFactory);
        init(restTemplate);
    }

    private void init(RestTemplate restTemplate) {
        setErrorHandler(restTemplate.getErrorHandler());
        setMessageConverters(restTemplate.getMessageConverters());
    }

    public RamlRestTemplate assumingBaseUri(String baseUri) {
        return new RamlRestTemplate(interceptor.getRamlDefinition(), baseUri, this);
    }

    void setRamlReport(RamlReport ramlReport) {
        this.lastReport = ramlReport;
    }

    public RamlReport getLastReport() {
        return lastReport;
    }
}
