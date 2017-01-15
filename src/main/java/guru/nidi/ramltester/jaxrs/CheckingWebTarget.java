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
package guru.nidi.ramltester.jaxrs;

import guru.nidi.ramltester.core.DummyReportAggragator;
import guru.nidi.ramltester.core.RamlChecker;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.ReportAggregator;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

public class CheckingWebTarget implements WebTarget {
    private final RamlChecker checker;
    private final WebTarget target;
    private RamlReport report;
    private ReportAggregator aggregator = new DummyReportAggragator();

    public CheckingWebTarget(RamlChecker checker, WebTarget target) {
        this.checker = checker;
        this.target = target;
        if (target.getConfiguration().getProperty("checked") != null) {
            throw new IllegalStateException("This WebTarget is already checking");
        }
        target.property("checked", true);
        target.register(new CheckingClientFilter(this));
    }

    void check(RamlRequest request, RamlResponse response) {
        report = checker.check(request, response);
        aggregator.addReport(report);
    }

    public RamlReport getLastReport() {
        return report;
    }

    public CheckingWebTarget aggregating(ReportAggregator aggregator) {
        this.aggregator = aggregator;
        return this;
    }

    @Override
    public URI getUri() {
        return target.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return target.getUriBuilder();
    }

    @Override
    public WebTarget path(String path) {
        return target.path(path);
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value) {
        return target.resolveTemplate(name, value);
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return target.resolveTemplate(name, value, encodeSlashInPath);
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String name, Object value) {
        return target.resolveTemplateFromEncoded(name, value);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues) {
        return target.resolveTemplates(templateValues);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        return target.resolveTemplates(templateValues, encodeSlashInPath);
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return target.resolveTemplatesFromEncoded(templateValues);
    }

    @Override
    public WebTarget matrixParam(String name, Object... values) {
        return target.matrixParam(name, values);
    }

    @Override
    public WebTarget queryParam(String name, Object... values) {
        return target.queryParam(name, values);
    }

    @Override
    public Invocation.Builder request() {
        return target.request();
    }

    @Override
    public Invocation.Builder request(String... acceptedResponseTypes) {
        return target.request(acceptedResponseTypes);
    }

    @Override
    public Invocation.Builder request(MediaType... acceptedResponseTypes) {
        return target.request(acceptedResponseTypes);
    }

    @Override
    public Configuration getConfiguration() {
        return target.getConfiguration();
    }

    @Override
    public WebTarget property(String name, Object value) {
        return target.property(name, value);
    }

    @Override
    public WebTarget register(Class<?> componentClass) {
        return target.register(componentClass);
    }

    @Override
    public WebTarget register(Class<?> componentClass, int priority) {
        return target.register(componentClass, priority);
    }

    @Override
    public WebTarget register(Class<?> componentClass, Class<?>... contracts) {
        return target.register(componentClass, contracts);
    }

    @Override
    public WebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return target.register(componentClass, contracts);
    }

    @Override
    public WebTarget register(Object component) {
        return target.register(component);
    }

    @Override
    public WebTarget register(Object component, int priority) {
        return target.register(component, priority);
    }

    @Override
    public WebTarget register(Object component, Class<?>... contracts) {
        return target.register(component, contracts);
    }

    @Override
    public WebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        return target.register(component, contracts);
    }
}
