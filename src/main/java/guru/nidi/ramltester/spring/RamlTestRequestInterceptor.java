/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.RamlTester;
import guru.nidi.ramltester.core.ReportStore;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 *
 */
public class RamlTestRequestInterceptor implements ClientHttpRequestInterceptor {
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
