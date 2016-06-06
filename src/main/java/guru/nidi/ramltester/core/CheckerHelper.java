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
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;

import java.io.Reader;
import java.util.*;

/**
 *
 */
final class CheckerHelper {
    private CheckerHelper() {
    }

    public static Protocol protocolOf(String s) {
        if ("http".equalsIgnoreCase(s)) {
            return Protocol.HTTP;
        }
        if ("https".equalsIgnoreCase(s)) {
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

    public static List<ResourceMatch> findResource(String resourcePath, Map<String, Resource> resources, Values values) {
        final List<ResourceMatch> matches = new ArrayList<>();
        for (final Map.Entry<String, Resource> entry : resources.entrySet()) {
            final VariableMatcher pathMatch = VariableMatcher.match(entry.getKey(), resourcePath);
            if (pathMatch.isCompleteMatch() || (pathMatch.isMatch() && pathMatch.getSuffix().startsWith("/"))) {
                matches.add(new ResourceMatch(pathMatch, entry.getValue()));
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
                found.addAll(findResource(match.match.getSuffix(), match.resource.getResources(), values));
            }
        }
        return found;
    }

    public static final class ResourceMatch implements Comparable<ResourceMatch> {
        final VariableMatcher match;
        final Resource resource;

        public ResourceMatch(VariableMatcher match, Resource resource) {
            this.match = match;
            this.resource = resource;
        }

        @Override
        public int compareTo(ResourceMatch o) {
            return match.getVariables().size() - o.match.getVariables().size();
        }
    }

    public static SchemaValidator findSchemaValidator(List<SchemaValidator> validators, MediaType mediaType) {
        for (final SchemaValidator validator : validators) {
            if (validator.supports(mediaType)) {
                return validator;
            }
        }
        return null;
    }

    public static Map<String, List<? extends AbstractParam>> getEffectiveBaseUriParams(Map<String, UriParameter> baseUriParams, Action action) {
        final Map<String, List<? extends AbstractParam>> params = new HashMap<>();
        if (action.getBaseUriParameters() != null) {
            params.putAll(action.getBaseUriParameters());
        }
        addNotSetBaseUriParams(action.getResource(), params);
        if (baseUriParams != null) {
            for (final Map.Entry<String, UriParameter> entry : baseUriParams.entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                }
            }
        }
        return params;
    }

    private static void addNotSetBaseUriParams(Resource resource, Map<String, List<? extends AbstractParam>> params) {
        if (resource.getBaseUriParameters() != null) {
            for (final Map.Entry<String, List<UriParameter>> entry : resource.getBaseUriParameters().entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (resource.getParentResource() != null) {
            addNotSetBaseUriParams(resource.getParentResource(), params);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<Map.Entry<String, AbstractParam>> paramEntries(Map<String, ?> params) {
        final List<Map.Entry<String, AbstractParam>> res = new ArrayList<>();
        for (final Map.Entry<String, ?> param : params.entrySet()) {
            if (param.getValue() instanceof List) {
                for (final AbstractParam p : (List<AbstractParam>) param.getValue()) {
                    res.add(new AbstractMap.SimpleEntry<>(param.getKey(), p));
                }
            } else {
                res.add((Map.Entry<String, AbstractParam>) param);
            }
        }
        return res;
    }

    public static Reader resolveSchema(Raml raml, String schema) {
        final String refSchema = raml.getConsolidatedSchemas().get(schema);
        return refSchema == null
                ? new NamedReader(schema, new Message("schema.inline").toString())
                : new NamedReader(refSchema, new Message("schema", schema).toString());
    }

    public static <T> Map<String, T> mergeMaps(Map<String, T> map1, Map<String, T> map2) {
        final Map<String, T> res = new HashMap<>();
        res.putAll(map1);
        res.putAll(map2);
        return res;
    }

}
