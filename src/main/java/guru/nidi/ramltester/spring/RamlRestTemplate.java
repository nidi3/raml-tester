package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlTester;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 *
 */
public class RamlRestTemplate extends RestTemplate implements RamlTestRequestInterceptor.ReportStore {
    private final ClientHttpRequestFactory originalRequestFactory;
    private final RamlTestRequestInterceptor interceptor;
    private RamlReport lastReport;

    public RamlRestTemplate(RamlTester tester, String baseUri, ClientHttpRequestFactory requestFactory) {
        originalRequestFactory = requestFactory;
        interceptor = new RamlTestRequestInterceptor(this, tester, baseUri);
        setRequestFactory(new InterceptingClientHttpRequestFactory(requestFactory, Collections.<ClientHttpRequestInterceptor>singletonList(interceptor)));
    }

    public RamlRestTemplate(RamlTester tester, String baseUri, RestTemplate restTemplate) {
        this(tester, baseUri, restTemplate.getRequestFactory());
        init(restTemplate);
    }

    public RamlRestTemplate(RamlTester tester, String baseUri, RamlRestTemplate restTemplate) {
        this(tester, baseUri, restTemplate.originalRequestFactory);
        init(restTemplate);
    }

    private void init(RestTemplate restTemplate) {
        setErrorHandler(restTemplate.getErrorHandler());
        setMessageConverters(restTemplate.getMessageConverters());
    }

    public RamlRestTemplate assumingBaseUri(String baseUri) {
        return new RamlRestTemplate(interceptor.getTester(), baseUri, this);
    }

    public RamlReport getLastReport() {
        return lastReport;
    }

    @Override
    public void storeReport(RamlReport report) {
        this.lastReport = report;
    }
}
