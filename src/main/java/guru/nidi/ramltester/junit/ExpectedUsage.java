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

import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.core.UsageItem;
import guru.nidi.ramltester.core.UsageProvider;
import guru.nidi.ramltester.model.Message;
import org.junit.rules.Verifier;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertThat;

/**
 *
 */
public class ExpectedUsage extends Verifier {
    private final UsageProvider usageProvider;
    private final EnumSet<UsageItem> items;

    public ExpectedUsage(UsageProvider usageProvider, UsageItem... items) {
        this.usageProvider = usageProvider;
        this.items = items.length == 0 ? EnumSet.allOf(UsageItem.class) : EnumSet.copyOf(Arrays.asList(items));
    }

    @Override
    protected void verify() throws Throwable {
        final Usage usage = usageProvider.getUsage();
        for (final UsageItem item : items) {
            assertThat(item.get(usage), new EmptyMatcher(new Message("usage." + item.name()).toString()));
        }
    }
}
