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
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.model.internal.RamlResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ResourceMatch implements Comparable<ResourceMatch> {
    final VariableMatcher match;
    final RamlResource resource;

    private ResourceMatch(VariableMatcher match, RamlResource resource) {
        this.match = match;
        this.resource = resource;
    }

    public static List<ResourceMatch> find(String resourcePath, List<RamlResource> resources, Values values) {
        final List<ResourceMatch> matches = new ArrayList<>();
        for (final RamlResource resource : resources) {
            final VariableMatcher pathMatch = VariableMatcher.match(resource.relativeUri(), resourcePath);
            if (pathMatch.isCompleteMatch() || (pathMatch.isMatch() && pathMatch.getSuffix().startsWith("/"))) {
                matches.add(new ResourceMatch(pathMatch, resource));
            }
        }
        Collections.sort(matches);
        final List<ResourceMatch> found = new ArrayList<>();
        for (final ResourceMatch match : matches) {
            if (match.match.isCompleteMatch()) {
                values.addValues(match.match.getVariables());
                found.add(match);
            } else if (match.match.isMatch()) {
                values.addValues(match.match.getVariables());
                found.addAll(find(match.match.getSuffix(), match.resource.resources(), values));
            }
        }
        return found;
    }

    @Override
    public int compareTo(ResourceMatch o) {
        return match.getVariables().size() - o.match.getVariables().size();
    }
}
