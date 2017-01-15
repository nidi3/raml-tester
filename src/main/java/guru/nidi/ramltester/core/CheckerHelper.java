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
import guru.nidi.ramltester.model.internal.*;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


final class CheckerHelper {
    private CheckerHelper() {
    }

    public static boolean isNoOrEmptyBodies(List<RamlBody> bodies) {
        return bodies == null || bodies.isEmpty();
    }

    public static boolean hasContent(RamlMessage message) {
        return message.getContent() != null && message.getContent().length > 0;
    }

    public static boolean existSchemalessBody(List<RamlBody> bodies) {
        for (final RamlBody mimeType : bodies) {
            if (mimeType.type() == null) {
                return true;
            }
        }
        return false;
    }

    public static RamlType findUriParam(String uriParam, RamlResource resource) {
        final RamlType param = typeByName(resource.uriParameters(), uriParam);
        if (param != null) {
            return param;
        }
        if (resource.parentResource() != null) {
            return findUriParam(uriParam, resource.parentResource());
        }
        return null;
    }

    public static List<ResourceMatch> findResource(String resourcePath, List<RamlResource> resources, Values values) {
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
                found.addAll(findResource(match.match.getSuffix(), match.resource.resources(), values));
            }
        }
        return found;
    }

    static final class ResourceMatch implements Comparable<ResourceMatch> {
        final VariableMatcher match;
        final RamlResource resource;

        public ResourceMatch(VariableMatcher match, RamlResource resource) {
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

    public static List<RamlType> getEffectiveBaseUriParams(List<RamlType> baseUriParams, RamlMethod action) {
        final List<RamlType> params = new ArrayList<>();
        if (action.baseUriParameters() != null) {
            params.addAll(action.baseUriParameters());
        }
        addNotSetBaseUriParams(action.resource(), params);
        if (baseUriParams != null) {
            for (final RamlType parameter : baseUriParams) {
                if (!typeNamesOf(params).contains(parameter.name())) {
                    params.add(parameter);
                }
            }
        }
        return params;
    }

    private static void addNotSetBaseUriParams(RamlResource resource, List<RamlType> params) {
        for (final RamlType parameter : resource.baseUriParameters()) {
            if (!typeNamesOf(params).contains(parameter.name())) {
                params.add(parameter);
            }
        }
        if (resource.parentResource() != null) {
            addNotSetBaseUriParams(resource.parentResource(), params);
        }
    }

    public static Reader resolveSchema(String type, String typeDef) {
        if (typeDef == null) {
            return new NamedReader(type, new Message("schema.inline").toString());
        }
        return type.equals(typeDef)
                ? new NamedReader(typeDef, new Message("schema.inline").toString())
                : new NamedReader(typeDef, new Message("schema", type).toString());
    }

    public static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        final List<T> res = new ArrayList<>();
        res.addAll(list1);
        res.addAll(list2);
        return res;
    }

    public static RamlApiResponse responseByCode(List<RamlApiResponse> responses, String code) {
        for (final RamlApiResponse response : responses) {
            if (response.code().equals(code)) {
                return response;
            }
        }
        return null;
    }

    public static RamlType typeByName(List<RamlType> types, String name) {
        final List<RamlType> res = typesByName(types, name);
        if (res.size() > 1) {
            throw new IllegalArgumentException("Expected only one parameter with given name " + name);
        }
        return res.isEmpty() ? null : res.get(0);
    }

    public static List<RamlType> typesByName(List<RamlType> types, String name) {
        final List<RamlType> res = new ArrayList<>();
        for (final RamlType type : types) {
            if (type.name().equals(name)) {
                res.add(type);
            }
        }
        return res;
    }

    public static List<String> typeNamesOf(List<RamlType> types) {
        final List<String> res = new ArrayList<>();
        for (final RamlType type : types) {
            res.add(type.name());
        }
        return res;
    }

    public static List<String> codesOf(List<RamlApiResponse> responses) {
        final List<String> res = new ArrayList<>();
        for (final RamlApiResponse response : responses) {
            res.add(response.code());
        }
        return res;
    }

}
