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
package guru.nidi.ramltester.httpcomponents;

import guru.nidi.ramltester.core.*;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 */
public class RamlHttpClient implements HttpClient, Closeable {
    private static final String RAML_TESTED = "raml.tested";
    private static final BasicHttpResponse DUMMY_RESPONSE = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NO_CONTENT, "not sending");

    private final RamlChecker checker;
    private final boolean notSending;
    private final ReportStore reportStore;
    private final CloseableHttpClient delegate;

    public RamlHttpClient(RamlChecker checker, boolean notSending, ReportStore reportStore, CloseableHttpClient delegate) {
        this.checker = checker;
        this.notSending = notSending;
        this.reportStore = reportStore;
        this.delegate = delegate;
    }

    public RamlHttpClient(RamlChecker checker) {
        this(checker, false, new ThreadLocalReportStore(), HttpClientBuilder.create().build());
    }

    public RamlHttpClient(RamlChecker checker, CloseableHttpClient httpClient) {
        this(checker, false, new ThreadLocalReportStore(), httpClient);
    }

    public RamlHttpClient notSending() {
        return new RamlHttpClient(checker, true, reportStore, delegate);
    }

    public RamlHttpClient aggregating(ReportAggregator aggregator) {
        return new RamlHttpClient(checker, notSending, new AggregatingReportStore(reportStore, aggregator), delegate);
    }

    public RamlReport getLastReport() {
        return reportStore.getLastReport();
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        reportStore.storeReport(null);
        final HttpComponentsRamlRequest ramlRequest = new HttpComponentsRamlRequest(target, request);
        final HttpResponse response;
        final RamlReport report;
        if (notSending) {
            response = DUMMY_RESPONSE;
            report = checker.check(ramlRequest);
        } else {
            response = delegate.execute(target, request, context);
            report = checker.check(ramlRequest, new HttpComponentsRamlResponse(response));
        }
        if (!alreadyTested(context)) {
            reportStore.storeReport(report);
        }
        return response;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        return execute(target, request, new BasicHttpContext());
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        reportStore.storeReport(null);
        final HttpComponentsRamlRequest ramlRequest = new HttpComponentsRamlRequest(request);
        final HttpResponse response;
        final RamlReport report;
        if (notSending) {
            response = DUMMY_RESPONSE;
            report = checker.check(ramlRequest);
        } else {
            response = delegate.execute(request, context);
            report = checker.check(ramlRequest, new HttpComponentsRamlResponse(response));
        }
        if (!alreadyTested(context)) {
            reportStore.storeReport(report);
        }
        return response;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException {
        return execute(request, new BasicHttpContext());
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        final T response = delegate.execute(request, responseHandler);
        return response;
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        final T response = delegate.execute(request, responseHandler, context);
        return response;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        final T response = delegate.execute(target, request, responseHandler);
        return response;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        final T response = delegate.execute(target, request, responseHandler, context);
        return response;
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        return delegate.getParams();
    }

    @Override
    @Deprecated
    public ClientConnectionManager getConnectionManager() {
        return delegate.getConnectionManager();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private boolean alreadyTested(HttpContext context) {
        if (context.getAttribute(RAML_TESTED) != null) {
            return true;
        }
        context.setAttribute(RAML_TESTED, true);
        return false;
    }
}
