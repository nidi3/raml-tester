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
package guru.nidi.ramltester.util;

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class FormDecoder {
    private static final String DEFAULT_CHARSET = "iso-8859-1";
    private static final MediaType MULTIPART = MediaType.valueOf("multipart/form-data");
    private static final MediaType URL_ENCODED = MediaType.valueOf("application/x-www-form-urlencoded");
    private static final Pattern QUERY_PARAM = Pattern.compile("([^&=]+)(=?)([^&]+)?");
    private static final int
            GROUP_NAME = 1,
            GROUP_EQUAL = 2,
            GROUP_VALUE = 3;

    public static boolean supportsFormParameters(MediaType mediaType) {
        return mediaType.isCompatibleWith(URL_ENCODED) || mediaType.isCompatibleWith(MULTIPART);
    }

    public Values decode(RamlRequest request) {
        if (request.getContentType() == null) {
            return new Values();
        }
        final MediaType type;
        try {
            type = MediaType.valueOf(request.getContentType());
        } catch (InvalidMediaTypeException e) {
            return new Values();
        }
        if (type.isCompatibleWith(URL_ENCODED)) {
            final String charset = type.getCharset(DEFAULT_CHARSET);
            try {
                final String content = IoUtils.readIntoString(new InputStreamReader(new ByteArrayInputStream(request.getContent()), charset));
                return decodeUrlEncoded(content, charset);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Unknown charset " + charset);
            } catch (IOException e) {
                throw new RuntimeException("Could not read request content", e);
            }
        }
        if (type.isCompatibleWith(MULTIPART)) {
            return decodeMultipart(request);
        }
        return new Values();
    }

    private static String charset(String contentType) {
        if (contentType == null) {
            return DEFAULT_CHARSET;
        }
        try {
            return MediaType.valueOf(contentType).getCharset(DEFAULT_CHARSET);
        } catch (InvalidMediaTypeException e) {
            return DEFAULT_CHARSET;
        }
    }

    private Values decodeMultipart(RamlRequest request) {
        try {
            final Values values = new Values();
            final RamlRequestFileUploadContext context = new RamlRequestFileUploadContext(request);
            final FileItemIterator iter = new ServletFileUpload().getItemIterator(context);
            while (iter.hasNext()) {
                final FileItemStream itemStream = iter.next();
                values.addValue(itemStream.getFieldName(), valueOf(itemStream));
            }
            return values;
        } catch (IOException | FileUploadException e) {
            throw new IllegalArgumentException("Could not parse multipart request", e);
        }
    }

    private Object valueOf(FileItemStream itemStream) throws IOException {
        if (itemStream.isFormField()) {
            final String charset = charset(itemStream.getContentType());
            return IoUtils.readIntoString(new InputStreamReader(itemStream.openStream(), charset));
        }
        return new FileValue();
    }

    private static class RamlRequestFileUploadContext implements RequestContext {
        private final RamlRequest request;

        private RamlRequestFileUploadContext(RamlRequest request) {
            this.request = request;
        }

        @Override
        public String getCharacterEncoding() {
            return charset(request.getContentType());
        }

        @Override
        public String getContentType() {
            return request.getContentType();
        }

        @Override
        public int getContentLength() {
            return request.getContent().length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(request.getContent());
        }
    }

    private Values decodeUrlEncoded(String content, String charset) {
        final Values q = new Values();
        if (content != null) {
            final Matcher m = QUERY_PARAM.matcher(content);
            while (m.find()) {
                final String name = urlDecode(m.group(GROUP_NAME), charset);
                final String eq = m.group(GROUP_EQUAL);
                final String value = m.group(GROUP_VALUE);
                q.addValue(name, (value != null ? urlDecode(value, charset) :
                        (eq != null && eq.length() > 0 ? "" : null)));
            }
        }
        return q;
    }

    private String urlDecode(String part, String charset) {
        try {
            return URLDecoder.decode(part, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unknown charset " + charset);
        }
    }

}
