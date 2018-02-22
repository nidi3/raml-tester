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

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.*;

import java.io.IOException;

import static guru.nidi.ramltester.junit.RamlMatchers.checks;
import static guru.nidi.ramltester.junit.RamlMatchers.validates;

@Ignore
//## httpComponents
public class HttpComponentsTest {
    @Test
    public void testRequest() throws IOException {
        RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
        Assert.assertThat(api.validate(), validates());

        RamlHttpClient client = api.createHttpClient();
        HttpGet get = new HttpGet("http://test.server/path");
        HttpResponse response = client.execute(get);

        Assert.assertThat(client.getLastReport(), checks());
    }
}
//##