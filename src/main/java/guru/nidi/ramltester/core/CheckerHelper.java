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

import guru.nidi.ramltester.model.*;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;

import java.io.Reader;
import java.util.*;

import static guru.nidi.ramltester.model.UnifiedModel.typeByName;
import static guru.nidi.ramltester.model.UnifiedModel.typeNamesOf;

/**
 *
 */
final class CheckerHelper {
    private CheckerHelper() {
    }

    public static boolean isNoOrEmptyBodies(List<UnifiedBody> bodies) {
        return bodies == null || bodies.isEmpty();
    }

    public static boolean hasContent(RamlMessage message) {
        return message.getContent() != null && message.getContent().length > 0;
    }

    public static boolean existSchemalessBody(List<UnifiedBody> bodies) {
        for (final UnifiedBody mimeType : bodies) {
            if (mimeType.type() == null) {
                return true;
            }
        }
        return false;
    }

    public static UnifiedType findUriParam(String uriParam, UnifiedResource resource) {
        final UnifiedType param = typeByName(resource.uriParameters(), uriParam);
        if (param != null) {
            return param;
        }
        if (resource.parentResource() != null) {
            return findUriParam(uriParam, resource.parentResource());
        }
        return null;
    }

    public static UnifiedResource findResource(String resourcePath, List<UnifiedResource> resources, Values values) {
        final List<ResourceMatch> matches = new ArrayList<>();
        for (final UnifiedResource resource : resources) {
            final VariableMatcher pathMatch = VariableMatcher.match(resource.relativeUri(), resourcePath);
            if (pathMatch.isCompleteMatch() || (pathMatch.isMatch() && pathMatch.getSuffix().startsWith("/"))) {
                matches.add(new ResourceMatch(pathMatch, resource));
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
                return findResource(match.match.getSuffix(), match.resource.resources(), values);
            }
        }
        return null;
    }

    private static final class ResourceMatch implements Comparable<ResourceMatch> {
        private final VariableMatcher match;
        private final UnifiedResource resource;

        public ResourceMatch(VariableMatcher match, UnifiedResource resource) {
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

    public static List<UnifiedType> getEffectiveBaseUriParams(List<UnifiedType> baseUriParams, UnifiedMethod action) {
        final List<UnifiedType> params = new ArrayList<>();
        if (action.baseUriParameters() != null) {
            params.addAll(action.baseUriParameters());
        }
        addNotSetBaseUriParams(action.resource(), params);
        if (baseUriParams != null) {
            for (final UnifiedType parameter : baseUriParams) {
                if (!typeNamesOf(params).contains(parameter.name())) {
                    params.add(parameter);
                }
            }
        }
        return params;
    }

    private static void addNotSetBaseUriParams(UnifiedResource resource, List<UnifiedType> params) {
        for (final UnifiedType parameter : resource.baseUriParameters()) {
            if (!typeNamesOf(params).contains(parameter.name())) {
                params.add(parameter);
            }
        }
        if (resource.parentResource() != null) {
            addNotSetBaseUriParams(resource.parentResource(), params);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<UnifiedType> paramEntries(List<UnifiedType> params) {
//        final List<Map.Entry<String, AbstractParam>> res = new ArrayList<>();
//        for (final Parameter param : params) {
//            if (param.getValue() instanceof List) {
//                for (final AbstractParam p : (List<AbstractParam>) param.getValue()) {
//                    res.add(new AbstractMap.SimpleEntry<>(param.getKey(), p));
//                }
//            } else {
//                res.add(param);
//            }
//        }
//        return res;
        return params;
    }

    public static Reader resolveSchema(String type, String typeDef) {
        return type.equals(typeDef)
                ? new NamedReader(typeDef, new Message("schema.inline").toString())
                : new NamedReader(typeDef, new Message("schema", type).toString());
    }

    public static <T> Map<String, T> mergeMaps(Map<String, T> map1, Map<String, T> map2) {
        final Map<String, T> res = new HashMap<>();
        res.putAll(map1);
        res.putAll(map2);
        return res;
    }

    public static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        final List<T> res = new ArrayList<>();
        res.addAll(list1);
        res.addAll(list2);
        return res;
    }

}
