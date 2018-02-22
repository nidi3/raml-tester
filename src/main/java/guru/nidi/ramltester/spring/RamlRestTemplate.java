/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.core.*;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class RamlRestTemplate extends RestTemplate {
    private final RamlChecker ramlChecker;
    private final boolean notSending;
    private final ReportStore reportStore;
    private final ClientHttpRequestFactory originalRequestFactory;

    private RamlRestTemplate(RamlChecker ramlChecker, boolean notSending, ReportStore reportStore,
                             ClientHttpRequestFactory requestFactory) {
        this.ramlChecker = ramlChecker;
        this.notSending = notSending;
        this.reportStore = reportStore;
        this.originalRequestFactory = requestFactory;
        final RamlRequestInterceptor interceptor = new RamlRequestInterceptor(ramlChecker, notSending, reportStore);
        setRequestFactory(new InterceptingClientHttpRequestFactory(
                new BufferingClientHttpRequestFactory(requestFactory), Collections.<ClientHttpRequestInterceptor>singletonList(interceptor)));
    }

    private RamlRestTemplate(RamlChecker ramlChecker, boolean notSending, ReportStore reportStore,
                             RamlRestTemplate restTemplate) {
        this(ramlChecker, notSending, reportStore, restTemplate.originalRequestFactory);
        init(restTemplate);
    }

    private RamlRestTemplate(RamlChecker ramlChecker, boolean notSending, ReportStore reportStore,
                             RestTemplate restTemplate) {
        this(ramlChecker, notSending, reportStore, restTemplate.getRequestFactory());
        init(restTemplate);
    }

    public RamlRestTemplate(RamlChecker ramlChecker, ClientHttpRequestFactory requestFactory) {
        this(ramlChecker, false, new ThreadLocalReportStore(), requestFactory);
    }

    public RamlRestTemplate(RamlChecker ramlChecker, RestTemplate restTemplate) {
        this(ramlChecker, false, new ThreadLocalReportStore(), restTemplate);
    }

    public RamlRestTemplate(RamlChecker ramlChecker, RamlRestTemplate restTemplate) {
        this(ramlChecker, false, new ThreadLocalReportStore(), restTemplate);
    }

    public RamlRestTemplate notSending() {
        return new RamlRestTemplate(ramlChecker, true, reportStore, this);
    }

    public RamlRestTemplate aggregating(ReportAggregator aggregator) {
        return new RamlRestTemplate(ramlChecker, notSending, new AggregatingReportStore(reportStore, aggregator), this);
    }

    private void init(RestTemplate restTemplate) {
        setErrorHandler(restTemplate.getErrorHandler());
        setMessageConverters(restTemplate.getMessageConverters());
    }

    public RamlReport getLastReport() {
        return reportStore.getLastReport();
    }
}
