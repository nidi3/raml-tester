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

import guru.nidi.ramltester.core.RamlChecker;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.ReportStore;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;

/**
 *
 */
public class RamlRequestInterceptor implements ClientHttpRequestInterceptor {
    private final RamlChecker checker;
    private final boolean notSending;
    private final ReportStore reportStore;

    public RamlRequestInterceptor(RamlChecker checker, boolean notSending, ReportStore reportStore) {
        this.checker = checker;
        this.notSending = notSending;
        this.reportStore = reportStore;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        reportStore.storeReport(null);
        final SpringHttpRequestRamlRequest ramlRequest = new SpringHttpRequestRamlRequest(request, body);
        final RamlReport report;
        final ClientHttpResponse response;
        if (notSending) {
            response = new MockClientHttpResponse((byte[]) null, HttpStatus.NO_CONTENT);
            report = checker.check(ramlRequest);
        } else {
            response = execution.execute(request, body);
            final SpringClientHttpResponseRamlResponse ramlResponse = new SpringClientHttpResponseRamlResponse(response);
            report = checker.check(ramlRequest, ramlResponse);
        }
        reportStore.storeReport(report);
        return response;
    }
}
