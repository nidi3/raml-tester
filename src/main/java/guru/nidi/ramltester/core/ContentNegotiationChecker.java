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

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.util.InvalidMediaTypeException;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.Action;
import org.raml.model.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
class ContentNegotiationChecker {
    private final RamlViolations requestViolations, responseViolations;

    public ContentNegotiationChecker(RamlViolations requestViolations, RamlViolations responseViolations) {
        this.requestViolations = requestViolations;
        this.responseViolations = responseViolations;
    }

    public void check(RamlRequest request, RamlResponse response, Action action, Response responseModel) {
        if (response.getContentType() == null || response.getContentType().isEmpty()) {
            return;
        }
        final List<Object> header = request.getHeaderValues().get("Accept");
        if (header == null || header.isEmpty()) {
            return;
        }
        final String accept = header.get(0).toString().trim();
        if (accept.length() == 0) {
            return;
        }
        final MediaType responseType;
        try {
            responseType = MediaType.valueOf(response.getContentType());
        } catch (InvalidMediaTypeException e) {
            return;  //violation is already created in RamlChecker
        }

        MediaType bestMatch = null;
        for (final MediaType acceptType : acceptMediaTypes(accept)) {
            for (final MediaType modelType : responseMediaTypes(responseModel)) {
                if (acceptType.isCompatibleWith(modelType)) {
                    if (bestMatch == null) {
                        bestMatch = acceptType;
                    }
                    if (responseType.equals(modelType)) {
                        if (acceptType.getQualityParameter() < bestMatch.getQualityParameter()) {
                            responseViolations.add(new Message("mediaType.better", accept, action, response.getStatus(), bestMatch, response.getContentType()));
                        }
                        return;
                    }
                }
            }
        }
        responseViolations.add(new Message("contentType.mismatch", accept, response.getContentType()));
    }

    private List<MediaType> acceptMediaTypes(String accept) {
        final List<MediaType> acceptTypes = new ArrayList<>();
        for (String type : accept.split(",")) {
            try {
                final MediaType acceptType = MediaType.valueOf(type);
                acceptTypes.add(acceptType);
            } catch (InvalidMediaTypeException e) {
                requestViolations.add(new Message("mediaType.illegal", type, e.getMessage()).withMessageParam("accept.header"));
            }
        }
        Collections.sort(acceptTypes, MediaType.QUALITY_COMPARATOR);
        return acceptTypes;
    }

    private List<MediaType> responseMediaTypes(Response responseModel) {
        final List<MediaType> modelTypes = new ArrayList<>();
        for (final String model : responseModel.getBody().keySet()) {
            try {
                modelTypes.add(MediaType.valueOf(model));
            } catch (InvalidMediaTypeException e) {
                requestViolations.add(new Message("mediaType.illegal", model, e.getMessage(), responseModel));
            }
        }
        return modelTypes;
    }

}
