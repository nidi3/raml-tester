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
import org.raml.model.Action;
import org.raml.model.MimeType;

import java.util.Map;

/**
 *
 */
final class CheckerType {
    private final MimeType mime;
    private final MediaType media;

    private CheckerType(MimeType mime, MediaType media) {
        this.mime = mime;
        this.media = media;
    }

    public MimeType getMime() {
        return mime;
    }

    public MediaType getMedia() {
        return media;
    }

    public String getCharset(){
        return media.getCharset("iso-8859-1");
    }

    public static CheckerType find(RamlViolations violations, Action action, RamlMessage message, Map<String, MimeType> bodies, String detail) {
        if (CheckerHelper.isNoOrEmptyBodies(bodies)) {
            violations.addIf(CheckerHelper.hasContent(message), "body.superfluous", action, detail);
            return null;
        }

        if (message.getContentType() == null) {
            violations.addIf(CheckerHelper.hasContent(message) || !CheckerHelper.existSchemalessBody(bodies), "contentType.missing");
            return null;
        }
        final MediaType targetType = MediaType.valueOf(message.getContentType());
        final MimeType mimeType = findMatchingMimeType(violations, action, bodies, targetType, detail);
        if (mimeType == null) {
            violations.add("mediaType.undefined", message.getContentType(), action, detail);
            return null;
        }
        return new CheckerType(mimeType, targetType);
    }

    private static MimeType findMatchingMimeType(RamlViolations violations, Action action, Map<String, MimeType> bodies, MediaType targetType, String detail) {
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
}
