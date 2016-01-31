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
import guru.nidi.ramltester.util.InvalidMediaTypeException;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.MimeType;

import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.*;

/**
 *
 */
final class MediaTypeMatch {
    private final MediaType targetType;
    private final Collection<MediaType> definedTypes;
    private final MediaType matchingMedia;
    private final MimeType matchingMime;

    private MediaTypeMatch(MediaType targetType, Collection<MediaType> definedTypes, MediaType matchingMedia, MimeType matchingMime) {
        this.targetType = targetType;
        this.definedTypes = definedTypes;
        this.matchingMedia = matchingMedia;
        this.matchingMime = matchingMime;
    }

    public MediaType getTargetType() {
        return targetType;
    }

    public Collection<MediaType> getDefinedTypes() {
        return definedTypes;
    }

    public MediaType getMatchingMedia() {
        return matchingMedia;
    }

    public MimeType getMatchingMime() {
        return matchingMime;
    }

    public String getTargetCharset() {
        return targetType.getCharset("iso-8859-1");
    }

    public static MediaTypeMatch find(RamlViolations violations, RamlMessage message, Map<String, MimeType> bodies, Locator locator) {
        if (isNoOrEmptyBodies(bodies)) {
            violations.addIf(hasContent(message), "body.superfluous", locator);
            return null;
        }

        if (message.getContentType() == null) {
            violations.addIf(hasContent(message) || !existSchemalessBody(bodies), "contentType.missing");
            return null;
        }
        final MediaType targetType;
        try {
            targetType = MediaType.valueOf(message.getContentType());
        } catch (InvalidMediaTypeException e) {
            violations.add("mediaType.illegal", locator, message.getContentType(), e.getMessage());
            return null;
        }
        final Map<MediaType, MimeType> mediaTypes = mediaTypes(violations, bodies, locator);
        final List<Map.Entry<MediaType, MimeType>> bestMatches = findBestMatches(mediaTypes, targetType);
        if (bestMatches.isEmpty()) {
            violations.add("mediaType.undefined", locator, message.getContentType());
            return null;
        }
        if (bestMatches.size() > 1) {
            violations.add("mediaType.ambiguous", locator, new Locator(bestMatches.get(0).getValue()), new Locator(bestMatches.get(1).getValue()));
            return null;
        }
        return new MediaTypeMatch(targetType, mediaTypes.keySet(), bestMatches.get(0).getKey(), bestMatches.get(0).getValue());
    }

    private static Map<MediaType, MimeType> mediaTypes(RamlViolations violations, Map<String, MimeType> bodies, Locator locator) {
        final Map<MediaType, MimeType> types = new LinkedHashMap<MediaType, MimeType>();
        for (final Map.Entry<String, MimeType> entry : bodies.entrySet()) {
            try {
                types.put(MediaType.valueOf(entry.getKey()), entry.getValue());
            } catch (InvalidMediaTypeException e) {
                violations.add(new Message("mediaType.illegal", locator, entry.getKey(), e.getMessage()));
            }
        }
        return types;
    }

    private static List<Map.Entry<MediaType, MimeType>> findBestMatches(Map<MediaType, MimeType> types, MediaType targetType) {
        final List<Map.Entry<MediaType, MimeType>> bestMatches = new ArrayList<Map.Entry<MediaType, MimeType>>();
        for (final Map.Entry<MediaType, MimeType> entry : types.entrySet()) {
            final int similarity = targetType.similarity(entry.getKey());
            if (bestMatches.isEmpty()) {
                if (similarity > 0) {
                    bestMatches.add(entry);
                }
            } else {
                final int bestSimilarity = targetType.similarity(bestMatches.get(0).getKey());
                if (similarity > bestSimilarity) {
                    bestMatches.clear();
                    bestMatches.add(entry);
                } else if (similarity == bestSimilarity) {
                    bestMatches.add(entry);
                }
            }
        }
        return bestMatches;
    }
}
