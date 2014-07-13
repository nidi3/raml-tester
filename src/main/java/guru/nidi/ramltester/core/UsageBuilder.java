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

import org.raml.model.*;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class UsageBuilder {
    private UsageBuilder() {
    }

    static Usage.Resource resourceUsage(Usage usage, Resource resource) {
        return usage.resource(resource.getUri());
    }

    static Usage.Action actionUsage(Usage usage, Action action) {
        return usage.resource(action.getResource().getUri()).action(action.getType().name());
    }

    static Usage.Response responseUsage(Usage usage, Action action, String responseCode) {
        return actionUsage(usage, action).response(responseCode);
    }

    static Usage.MimeType mimeTypeUsage(Usage usage, Action action, MimeType mimeType) {
        return actionUsage(usage, action).mimeType(mimeType.getType());
    }

    public static Usage usage(Raml raml, List<RamlReport> reports) {
        final Usage usage = new Usage();
        createTotalUsage(usage, raml.getResources());
        for (RamlReport report : reports) {
            usage.add(report.getUsage());
        }
        return usage;
    }

    private static void createTotalUsage(Usage usage, Map<String, Resource> resources) {
        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            resourceUsage(usage, resourceEntry.getValue());
            for (Action action : resourceEntry.getValue().getActions().values()) {
                actionUsage(usage, action).initQueryParameters(action.getQueryParameters().keySet());
                actionUsage(usage, action).initResponseCodes(action.getResponses().keySet());
                actionUsage(usage, action).initRequestHeaders(action.getHeaders().keySet());
                if (action.getBody() != null) {
                    for (MimeType mimeType : action.getBody().values()) {
                        if (mimeType.getFormParameters() != null) {
                            UsageBuilder.mimeTypeUsage(usage, action, mimeType).initFormParameters(mimeType.getFormParameters().keySet());
                        }
                    }
                }
                for (Map.Entry<String, Response> responseEntry : action.getResponses().entrySet()) {
                    responseUsage(usage, action, responseEntry.getKey()).initResponseHeaders(responseEntry.getValue().getHeaders().keySet());
                }
            }
            createTotalUsage(usage, resourceEntry.getValue().getResources());
        }
    }
}
