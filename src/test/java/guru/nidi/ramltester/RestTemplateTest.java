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
package guru.nidi.ramltester;

import guru.nidi.ramltester.junit.ExpectedUsage;
import guru.nidi.ramltester.model.RamlViolations;
import guru.nidi.ramltester.spring.RamlRestTemplate;
import guru.nidi.ramltester.util.ServerTest;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestTemplateTest.class)
@Controller
@Configuration
public class RestTemplateTest extends ServerTest {

    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();

    private static RamlRestTemplate restTemplate = RamlLoaders
            .fromClasspath(RestTemplateTest.class)
            .load("template.raml")
            .assumingBaseUri("http://nidi.guru/raml/v1")
            .createRestTemplate(new HttpComponentsClientHttpRequestFactory())
            .aggregating(aggregator);

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @RequestMapping(value = "/data")
    @ResponseBody
    public HttpEntity<String> test(@RequestParam(required = false) String param) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(param == null ? "\"json string\"" : "illegal json", headers);
    }

    @Override
    protected int port() {
        return 8081;
    }

    @Override
    protected void init(Context ctx) {
        AnnotationConfigWebApplicationContext appCtx = new AnnotationConfigWebApplicationContext();
        appCtx.register(getClass());
        DispatcherServlet dispatcher = new DispatcherServlet(appCtx);
        Tomcat.addServlet(ctx, "SpringMVC", dispatcher);
        ctx.addServletMapping("/*", "SpringMVC");
    }

    @Test
    public void testRestTemplateOk() {
        final String res = restTemplate.getForObject(url("data"), String.class);
        assertEquals("\"json string\"", res);
        assertTrue(restTemplate.getLastReport().isEmpty());
    }

    @Test
    public void testRestTemplateNok() {
        final String res = restTemplate.getForObject(url("data?param=bu"), String.class);
        assertEquals("illegal json", res);

        final RamlViolations requestViolations = restTemplate.getLastReport().getRequestViolations();
        assertEquals(1, requestViolations.size());
        assertThat(requestViolations.iterator().next(), equalTo("Query parameter 'param' on action(GET /data) is not defined"));

        final RamlViolations responseViolations = restTemplate.getLastReport().getResponseViolations();
        assertEquals(1, responseViolations.size());
        assertThat(responseViolations.iterator().next(),
                startsWith("Body does not match schema for action(GET /data) response(200) mime-type('application/json')\n" +
                        "Content: illegal json\n" +
                        "Message: Schema invalid: ")
        );
    }

}