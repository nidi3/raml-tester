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
package guru.nidi.ramltester.snippets;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static guru.nidi.ramltester.junit.RamlMatchers.checks;
import static guru.nidi.ramltester.junit.RamlMatchers.validates;
import static org.junit.Assert.assertThat;

@Ignore
//## jaxrs
@RunWith(Arquillian.class)
public class JaxrsTest {

    private static RamlDefinition api = RamlLoaders.fromClasspath(JaxrsTest.class).load("api.raml")
            .assumingBaseUri("http://nidi.guru/raml/simple/v1");
    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();
    private static WebTarget target;

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(Application.class);
    }

    @ArquillianResource
    private URL base;

    @Before
    public void setup() throws MalformedURLException {
        Client client = ClientBuilder.newClient();
        target = client.target(URI.create(new URL(base, "app/path").toExternalForm()));
    }

    @Test
    public void greeting() throws Exception {
        assertThat(api.validate(), validates());

        final CheckingWebTarget webTarget = api.createWebTarget(target).aggregating(aggregator);
        webTarget.request().post(Entity.text("apple"));

        assertThat(webTarget.getLastReport(), checks());
    }
}
//##