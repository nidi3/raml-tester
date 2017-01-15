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
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v08.api.DocumentationItem;

import java.util.ArrayList;
import java.util.List;

public class DocItem08 implements RamlDocItem {
    private final DocumentationItem item;

    DocItem08(DocumentationItem item) {
        this.item = item;
    }

    static List<RamlDocItem> of(List<DocumentationItem> items) {
        final List<RamlDocItem> res = new ArrayList<>();
        for (final DocumentationItem i : items) {
            res.add(new DocItem08(i));
        }
        return res;
    }

    @Override
    public String title() {
        return item.title();
    }

    @Override
    public String content() {
        return item.content().value();
    }
}
