package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlTester;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 *
 */
public class RamlTestRequestInterceptor implements ClientHttpRequestInterceptor {
    public interface ReportStore {
        void storeReport(RamlReport report);
    }

    private final ReportStore reportStore;
    private final RamlTester tester;
    private final String baseUri;

    public RamlTestRequestInterceptor(ReportStore reportStore, RamlTester tester, String baseUri) {
        this.reportStore = reportStore;
        this.tester = tester;
        this.baseUri = baseUri;
    }

    public RamlTester getTester() {
        return tester;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        reportStore.storeReport(null);
        final ClientHttpResponse response = execution.execute(request, body);
        final SpringClientHttpResponseRamlResponse ramlResponse = new SpringClientHttpResponseRamlResponse(response);
        final SpringHttpRequestRamlRequest ramlRequest = new SpringHttpRequestRamlRequest(request, baseUri, body);
        reportStore.storeReport(tester.test(ramlRequest, ramlResponse));
        return ramlResponse;
    }
}
