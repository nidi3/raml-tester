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

import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.bodies.BodyLike;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.resources.Resource;

import java.util.List;

import static guru.nidi.ramltester.core.CheckerHelper.codesOf;
import static guru.nidi.ramltester.core.CheckerHelper.namesOf;

/**
 *
 */
public final class UsageBuilder {
    private UsageBuilder() {
    }

    static Usage.Resource resourceUsage(Usage usage, Resource resource) {
        return usage.resource(resource.resourcePath());
    }

    static Usage.Action actionUsage(Usage usage, Method action) {
        return usage.resource(action.resource().resourcePath()).action(action.method());
    }

    static Usage.Response responseUsage(Usage usage, Method action, String responseCode) {
        return actionUsage(usage, action).response(responseCode);
    }

    static Usage.MimeType mimeTypeUsage(Usage usage, Method action, BodyLike mimeType) {
        return actionUsage(usage, action).mimeType(mimeType.name());
    }

    public static Usage usage(Api raml, List<RamlReport> reports) {
        final Usage usage = new Usage();
        createTotalUsage(usage, raml.resources());
        for (final RamlReport report : reports) {
            usage.add(report.getUsage());
        }
        return usage;
    }

    private static void createTotalUsage(Usage usage, List<Resource> resources) {
        for (final Resource resource : resources) {
            resourceUsage(usage, resource);
            for (final Method action : resource.methods()) {
                actionUsage(usage, action).initQueryParameters(namesOf(action.queryParameters()));
                actionUsage(usage, action).initResponseCodes(codesOf(action.responses()));
                actionUsage(usage, action).initRequestHeaders(namesOf(action.headers()));
                if (action.body() != null) {
                    for (final BodyLike mimeType : action.body()) {
                        if (mimeType.formParameters() != null) {
                            UsageBuilder.mimeTypeUsage(usage, action, mimeType).initFormParameters(namesOf(mimeType.formParameters()));
                        }
                    }
                }
                for (final Response response : action.responses()) {
                    responseUsage(usage, action, response.code().value()).initResponseHeaders(namesOf(response.headers()));
                }
            }
            createTotalUsage(usage, resource.resources());
        }
    }
}
