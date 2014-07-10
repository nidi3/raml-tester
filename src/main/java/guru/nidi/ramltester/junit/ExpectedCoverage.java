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
package guru.nidi.ramltester.junit;

import guru.nidi.ramltester.CoverageProvider;
import guru.nidi.ramltester.core.CoverageItem;
import guru.nidi.ramltester.core.Message;
import guru.nidi.ramltester.core.RamlCoverage;
import org.junit.rules.Verifier;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertThat;

/**
 *
 */
public class ExpectedCoverage extends Verifier {
    private final CoverageProvider coverageProvider;
    private final EnumSet<CoverageItem> items;

    public ExpectedCoverage(CoverageProvider coverageProvider, CoverageItem... items) {
        this.coverageProvider = coverageProvider;
        this.items = items.length == 0 ? EnumSet.allOf(CoverageItem.class) : EnumSet.copyOf(Arrays.asList(items));
    }

    @Override
    protected void verify() throws Throwable {
        final RamlCoverage coverage = coverageProvider.getCoverage();
        for (CoverageItem item : items) {
            assertThat(item.get(coverage), new EmptyMatcher(new Message("coverage." + item.name()).toString()));
        }
    }
}
