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
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.bodies.BodyLike;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.resources.Resource;

import java.io.Reader;
import java.util.*;

/**
 *
 */
final class CheckerHelper {
    private CheckerHelper() {
    }

    public static boolean isNoOrEmptyBodies(List<BodyLike> bodies) {
        return bodies == null || bodies.isEmpty();
    }

    public static boolean hasContent(RamlMessage message) {
        return message.getContent() != null && message.getContent().length > 0;
    }

    public static boolean existSchemalessBody(List<BodyLike> bodies) {
        for (final BodyLike mimeType : bodies) {
            if (mimeType.schema() == null) {
                return true;
            }
        }
        return false;
    }

    public static Parameter findUriParam(String uriParam, Resource resource) {
        final Parameter param = paramByName(resource.uriParameters(), uriParam);
        if (param != null) {
            return param;
        }
        if (resource.parentResource() != null) {
            return findUriParam(uriParam, resource.parentResource());
        }
        return null;
    }

    public static Resource findResource(String resourcePath, List<Resource> resources, Values values) {
        final List<ResourceMatch> matches = new ArrayList<>();
        for (final Resource resource : resources) {
            final VariableMatcher pathMatch = VariableMatcher.match(resource.relativeUri().value(), resourcePath);
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
        private final Resource resource;

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

    public static List<Parameter> getEffectiveBaseUriParams(List<Parameter> baseUriParams, Method action) {
        final List<Parameter> params = new ArrayList<>();
        if (action.baseUriParameters() != null) {
            params.addAll(action.baseUriParameters());
        }
        addNotSetBaseUriParams(action.resource(), params);
        if (baseUriParams != null) {
            for (final Parameter parameter : baseUriParams) {
                if (!namesOf(params).contains(parameter.name())) {
                    params.add(parameter);
                }
            }
        }
        return params;
    }

    private static void addNotSetBaseUriParams(Resource resource, List<Parameter> params) {
        if (resource.baseUriParameters() != null) {
            for (final Parameter parameter : resource.baseUriParameters()) {
                if (!namesOf(params).contains(parameter.name())) {
                    params.add(parameter);
                }
            }
        }
        if (resource.parentResource() != null) {
            addNotSetBaseUriParams(resource.parentResource(), params);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Parameter> paramEntries(List<Parameter> params) {
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

    public static Reader resolveSchema(Api raml, String schema) {
//        final String refSchema = raml.getConsolidatedSchemas().get(schema);
        String refSchema = schema;
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

    public static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        final List<T> res = new ArrayList<>();
        res.addAll(list1);
        res.addAll(list2);
        return res;
    }

    public static List<String> namesOf(List<Parameter> params) {
        final List<String> res = new ArrayList<>();
        for (final Parameter param : params) {
            res.add(param.name());
        }
        return res;
    }

    public static List<String> codesOf(List<Response> params) {
        final List<String> res = new ArrayList<>();
        for (final Response param : params) {
            res.add(param.code().value());
        }
        return res;
    }

    public static Parameter paramByName(List<Parameter> parameters, String name) {
        final List<Parameter> res = paramsByName(parameters, name);
        if (res.size() > 1) {
            throw new IllegalArgumentException("Expected only one parameter with given name " + name);
        }
        return res.isEmpty() ? null : res.get(0);
    }

    public static List<Parameter> paramsByName(List<Parameter> parameters, String name) {
        final List<Parameter> res = new ArrayList<>();
        for (final Parameter parameter : parameters) {
            if (parameter.name().equals(name)) {
                res.add(parameter);
            }
        }
        return res;
    }

    public static Response responseByCode(List<Response> responses, String code) {
        for (final Response response : responses) {
            if (response.code().value().equals(code)) {
                return response;
            }
        }
        return null;
    }

}
