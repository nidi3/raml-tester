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

import guru.nidi.ramltester.core.SimpleReport;
import guru.nidi.ramltester.core.Usage;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 *
 */
public class MultiReportAggregatorTest {
    @Test
    public void simple() {
        final MultiReportAggregator aggregator = new MultiReportAggregator();
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
        final MultiReportAggregator aggregator = new MultiReportAggregator();
        aggregator.addReport(SimpleReport.report("simple.raml", "/data", "/d", "/"));
        aggregator.clear();
        assertFalse(aggregator.usages().iterator().hasNext());
    }

    @Test
    public void multi() {
        final MultiReportAggregator aggregator = new MultiReportAggregator();
        aggregator.addReport(SimpleReport.report("simple.raml"));
        aggregator.addReport(SimpleReport.report("header.raml"));
        final Iterator<Map.Entry<String, Usage>> usages = aggregator.usages().iterator();
        assertThat(usages.next().getKey(), either(equalTo("simple")).or(equalTo("header")));
        assertThat(usages.next().getKey(), either(equalTo("simple")).or(equalTo("header")));
        assertFalse(usages.hasNext());
    }
}
