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

import guru.nidi.ramltester.core.SimpleReport;
import guru.nidi.ramltester.core.Usage;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class SimpleReportAggregatorTest {
    @Test
    public void simple() {
        final SimpleReportAggregator aggregator = new SimpleReportAggregator();
        aggregator.addReport(SimpleReport.report("simple.raml", "/data", "/d", "/"));
        final Iterator<Map.Entry<String, Usage>> usages = aggregator.usages().iterator();
        final Map.Entry<String, Usage> usageEntry = usages.next();
        assertEquals("simple", usageEntry.getKey());
        assertEquals(new HashSet<>(Arrays.asList("/mediaType", "/schema")),
                usageEntry.getValue().getUnusedResources());
        assertFalse(usages.hasNext());
    }

    @Test
    public void clear() {
        final SimpleReportAggregator aggregator = new SimpleReportAggregator();
        aggregator.addReport(SimpleReport.report("simple.raml", "/data", "/d", "/"));
        aggregator.clear();
        assertFalse(aggregator.usages().iterator().hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiRamlDisallowed() {
        final SimpleReportAggregator aggregator = new SimpleReportAggregator();
        aggregator.addReport(SimpleReport.report("simple.raml"));
        aggregator.addReport(SimpleReport.report("header.raml"));
    }
}
