/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.internal.*;

import java.util.List;

import static guru.nidi.ramltester.core.CheckerHelper.codesOf;
import static guru.nidi.ramltester.core.CheckerHelper.typeNamesOf;


public final class UsageBuilder {
    private UsageBuilder() {
    }

    static Usage.Resource resourceUsage(Usage usage, RamlResource resource) {
        return usage.resource(resource.resourcePath());
    }

    static Usage.Method methodUsage(Usage usage, RamlMethod method) {
        return usage.resource(method.resource().resourcePath()).method(method.method());
    }

    static Usage.Response responseUsage(Usage usage, RamlMethod method, String responseCode) {
        return methodUsage(usage, method).response(responseCode);
    }

    static Usage.Body bodyUsage(Usage usage, RamlMethod method, RamlBody body) {
        return methodUsage(usage, method).body(body.name());
    }

    public static Usage usage(RamlApi raml, List<RamlReport> reports) {
        final Usage usage = new Usage();
        createTotalUsage(usage, raml.resources());
        for (final RamlReport report : reports) {
            usage.add(report.getUsage());
        }
        return usage;
    }

    private static void createTotalUsage(Usage usage, List<RamlResource> resources) {
        for (final RamlResource resource : resources) {
            resourceUsage(usage, resource);
            for (final RamlMethod method : resource.methods()) {
                methodUsage(usage, method).initQueryParameters(typeNamesOf(method.queryParameters()));
                methodUsage(usage, method).initResponseCodes(codesOf(method.responses()));
                methodUsage(usage, method).initRequestHeaders(typeNamesOf(method.headers()));
                if (method.body() != null) {
                    for (final RamlBody body : method.body()) {
                        if (!body.formParameters().isEmpty()) {
                            UsageBuilder.bodyUsage(usage, method, body).initParameters(typeNamesOf(body.formParameters()));
                        }
                    }
                }
                for (final RamlApiResponse response : method.responses()) {
                    responseUsage(usage, method, response.code()).initResponseHeaders(typeNamesOf(response.headers()));
                }
            }
            createTotalUsage(usage, resource.resources());
        }
    }
}
