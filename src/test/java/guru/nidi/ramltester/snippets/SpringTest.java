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
package guru.nidi.ramltester.snippets;

import guru.nidi.ramltester.*;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static guru.nidi.ramltester.junit.RamlMatchers.validates;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Ignore
//## spring
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class SpringTest {

    private static RamlDefinition api = RamlLoaders.fromClasspath(SpringTest.class).load("api.raml")
            .assumingBaseUri("http://nidi.guru/raml/simple/v1");
    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void greeting() throws Exception {
        Assert.assertThat(api.validate(), validates());

        mockMvc.perform(get("/greeting").accept(MediaType.parseMediaType("application/json")))
                .andExpect(api.matches().aggregating(aggregator));
    }
}
//##