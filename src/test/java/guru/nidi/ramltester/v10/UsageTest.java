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
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.core.Usage;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class UsageTest extends HighlevelTestBase {
    private static final RamlDefinition api = RamlLoaders.fromClasspath(SimpleTest.class).load("usage.raml");

    @Test
    public void findUnused() throws Exception {
        final SimpleReportAggregator aggregator = new SimpleReportAggregator();
        assertThat(test(aggregator, api, get("/data"), jsonResponse(200, "\"hula\"")),
                hasNoViolations());

        final Usage usage = aggregator.getUsage();
        assertEquals(set("/uuWithAction"), usage.getUnusedResources());
        assertEquals(set("POST /data", "GET /uuWithAction"), usage.getUnusedActions());
        assertEquals(set("uuQuery in GET /data"), usage.getUnusedQueryParameters());
        assertEquals(set("uuReqHeader in GET /data"), usage.getUnusedRequestHeaders());
        assertEquals(set("uuResHeader in GET /data -> 200"), usage.getUnusedResponseHeaders());
        assertEquals(set("201 in GET /data"), usage.getUnusedResponseCodes());
    }

    private Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
