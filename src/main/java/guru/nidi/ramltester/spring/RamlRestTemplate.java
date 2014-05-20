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
import guru.nidi.ramltester.core.ThreadLocalReportStore;
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
    private final ReportStore reportStore = new ThreadLocalReportStore();

    public RamlRestTemplate(RamlChecker checker, ClientHttpRequestFactory requestFactory) {
        originalRequestFactory = requestFactory;
        interceptor = new RamlTestRequestInterceptor(reportStore, checker);
        setRequestFactory(new InterceptingClientHttpRequestFactory(requestFactory, Collections.<ClientHttpRequestInterceptor>singletonList(interceptor)));
    }

    public RamlRestTemplate(RamlChecker checker, RestTemplate restTemplate) {
        this(checker, restTemplate.getRequestFactory());
        init(restTemplate);
    }

    public RamlRestTemplate(RamlChecker checker, RamlRestTemplate restTemplate) {
        this(checker, restTemplate.originalRequestFactory);
        init(restTemplate);
    }

    private void init(RestTemplate restTemplate) {
        setErrorHandler(restTemplate.getErrorHandler());
        setMessageConverters(restTemplate.getMessageConverters());
    }

    public RamlReport getLastReport() {
        return reportStore.getLastReport();
    }
}
