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
package guru.nidi.ramltester;

import org.junit.Test;
import org.raml.model.*;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;


/**
 * Wait for https://github.com/raml-org/raml-java-parser/issues/80 to be fixed.
 */
public class IncludeTest extends HighlevelTestBase {
    private final RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("include.raml");

    @Test
    public void load() throws Exception {
        final Raml raml = api.getRaml();
        final Action get = raml.getResource("/site").getAction(ActionType.GET);
        assertNull(get.getQueryParameters().get("string.json"));
        assertNotEquals("string.json", get.getResponses().get("200").getBody().get("application/json").getExample());
        assertNotEquals("string.json", get.getResponses().get("201").getBody().get("application/json").getSchema());
        assertNotEquals("string.json", get.getResponses().get("201").getBody().get("application/json").getExample());
    }


}
