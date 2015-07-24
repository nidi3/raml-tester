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

import guru.nidi.ramltester.model.RamlMessage;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.InvalidMediaTypeException;
import guru.nidi.ramltester.util.MediaType;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Protocol;
import org.raml.model.Resource;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
final class CheckerHelper {
    private CheckerHelper() {
    }

    public static Protocol protocolOf(String s) {
        if (s.equalsIgnoreCase("http")) {
            return Protocol.HTTP;
        }
        if (s.equalsIgnoreCase("https")) {
            return Protocol.HTTPS;
        }
        return null;
    }

    public static boolean isNoOrEmptyBodies(Map<String, MimeType> bodies) {
        return bodies == null || bodies.isEmpty() || (bodies.size() == 1 && bodies.containsKey(null));
    }

    public static boolean hasContent(RamlMessage message) {
        return message.getContent() != null && message.getContent().length > 0;
    }

    public static boolean existSchemalessBody(Map<String, MimeType> bodies) {
        for (final MimeType mimeType : bodies.values()) {
            if (mimeType.getSchema() == null) {
                return true;
            }
        }
        return false;
    }

    public static MimeType findMatchingMimeType(RamlViolations violations, Action action, Map<String, MimeType> bodies, MediaType targetType, String detail) {
        MimeType res = null;
        try {
            for (final Map.Entry<String, MimeType> entry : bodies.entrySet()) {
                if (targetType.isCompatibleWith(MediaType.valueOf(entry.getKey()))) {
                    if (res == null) {
                        res = entry.getValue();
                    } else {
                        violations.add("mediaType.ambiguous", res, entry.getValue(), action, detail);
                    }
                }
            }
        } catch (InvalidMediaTypeException e) {
            violations.add("mediaType.illegal", e.getMimeType());
        }
        return res;
    }

    public static AbstractParam findUriParam(String uriParam, Resource resource) {
        final UriParameter param = resource.getUriParameters().get(uriParam);
        if (param != null) {
            return param;
        }
        if (resource.getParentResource() != null) {
            return findUriParam(uriParam, resource.getParentResource());
        }
        return null;
    }

    public static Resource findResource(String resourcePath, Map<String, Resource> resources, Values values) {
        final List<ResourceMatch> matches = new ArrayList<>();
        for (final Map.Entry<String, Resource> entry : resources.entrySet()) {
            final VariableMatcher pathMatch = VariableMatcher.match(entry.getKey(), resourcePath);
            if (pathMatch.isCompleteMatch() || (pathMatch.isMatch() && pathMatch.getSuffix().startsWith("/"))) {
                matches.add(new ResourceMatch(pathMatch, entry.getValue()));
            }
        }
        Collections.sort(matches);
        for (final ResourceMatch match : matches) {
            if (match.match.isCompleteMatch()) {
                values.addValues(match.match.getVariables());
                return match.resource;
            }
            if (match.match.isMatch()) {
                values.addValues(match.match.getVariables());
                return findResource(match.match.getSuffix(), match.resource.getResources(), values);
            }
        }
        return null;
    }

    private static final class ResourceMatch implements Comparable<ResourceMatch> {
        private final VariableMatcher match;
        private final Resource resource;

        private ResourceMatch(VariableMatcher match, Resource resource) {
            this.match = match;
            this.resource = resource;
        }

        @Override
        public int compareTo(ResourceMatch o) {
            return match.getVariables().size() - o.match.getVariables().size();
        }
    }

}
