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

import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlTester;
import guru.nidi.ramltester.core.ReportStore;
import guru.nidi.ramltester.core.ThreadLocalReportStore;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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

    private final RamlTester tester;
    private final CloseableHttpClient delegate;
    private final ReportStore reportStore = new ThreadLocalReportStore();

    public RamlHttpClient(RamlTester tester, CloseableHttpClient delegate) {
        this.tester = tester;
        this.delegate = delegate;
    }

    public RamlHttpClient(RamlTester tester) {
        this(tester, HttpClientBuilder.create().build());
    }

    public RamlReport getLastReport() {
        return reportStore.getLastReport();
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        if (alreadyTested(context)) {
            return delegate.execute(target, request, context);
        }
        reportStore.storeReport(null);
        final CloseableHttpResponse response = delegate.execute(target, request, context);
        reportStore.storeReport(tester.test(new HttpComponentsRamlRequest(target, request), new HttpComponentsRamlResponse(response)));
        return response;
    }


    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        if (alreadyTested(context)) {
            return delegate.execute(request, context);
        }
        reportStore.storeReport(null);
        final HttpResponse response = delegate.execute(request, context);
        reportStore.storeReport(tester.test(new HttpComponentsRamlRequest(request), new HttpComponentsRamlResponse(response)));
        return response;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException {
        final BasicHttpContext context = new BasicHttpContext();
        final HttpResponse response = delegate.execute(request, context);
        if (!alreadyTested(context)) {
            reportStore.storeReport(tester.test(new HttpComponentsRamlRequest(request), new HttpComponentsRamlResponse(response)));
        }
        return response;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        final BasicHttpContext context = new BasicHttpContext();
        final HttpResponse response = delegate.execute(target, request, context);
        if (!alreadyTested(context)) {
            reportStore.storeReport(tester.test(new HttpComponentsRamlRequest(target, request), new HttpComponentsRamlResponse(response)));
        }
        return response;
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
