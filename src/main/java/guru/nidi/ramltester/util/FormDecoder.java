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

import guru.nidi.ramltester.model.Values;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.util.IoUtils.readIntoString;
import static guru.nidi.ramltester.util.MediaType.FORM_URL_ENCODED;
import static guru.nidi.ramltester.util.MediaType.MULTIPART;

public class FormDecoder {
    private static final String DEFAULT_CHARSET = "iso-8859-1";
    private static final Pattern QUERY_PARAM = Pattern.compile("([^&=]+)(=?)([^&]+)?");
    private static final int
            GROUP_NAME = 1,
            GROUP_EQUAL = 2,
            GROUP_VALUE = 3;

    private final byte[] content;
    private final MediaType contentType;

    public FormDecoder(byte[] content, String contentType) {
        this(content, parseContentType(contentType));
    }

    public FormDecoder(byte[] content, MediaType contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    private static MediaType parseContentType(String contentType) {
        try {
            return MediaType.valueOf(contentType);
        } catch (InvalidMediaTypeException e) {
            return null;
        }
    }

    public boolean supportsFormParameters() {
        return contentType.isCompatibleWith(FORM_URL_ENCODED) || contentType.isCompatibleWith(MULTIPART);
    }

    public Values decode() {
        if (contentType == null) {
            return new Values();
        }
        if (contentType.isCompatibleWith(FORM_URL_ENCODED)) {
            final String charset = contentType.getCharset(DEFAULT_CHARSET);
            try {
                final String data = readIntoString(new InputStreamReader(new ByteArrayInputStream(content), charset));
                return decodeUrlEncoded(data, charset);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Unknown charset " + charset, e);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        if (contentType.isCompatibleWith(MULTIPART)) {
            return decodeMultipart();
        }
        return new Values();
    }

    private Values decodeUrlEncoded(String content, String charset) {
        final Values q = new Values();
        if (content != null) {
            final Matcher m = QUERY_PARAM.matcher(content);
            while (m.find()) {
                final String name = urlDecode(m.group(GROUP_NAME), charset);
                final String eq = m.group(GROUP_EQUAL);
                final String value = m.group(GROUP_VALUE);
                q.addValue(name, value == null
                        ? (eq != null && eq.length() > 0 ? "" : null)
                        : urlDecode(value, charset));
            }
        }
        return q;
    }

    private String urlDecode(String part, String charset) {
        try {
            return URLDecoder.decode(part, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unknown charset " + charset, e);
        }
    }

    private Values decodeMultipart() {
        try {
            final Values values = new Values();
            final RamlRequestFileUploadContext context = new RamlRequestFileUploadContext();
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
            return readIntoString(new InputStreamReader(itemStream.openStream(), charset));
        }
        return new FileValue();
    }

    private static String charset(String contentType) {
        try {
            return MediaType.valueOf(contentType).getCharset(DEFAULT_CHARSET);
        } catch (InvalidMediaTypeException e) {
            return DEFAULT_CHARSET;
        }
    }

    private class RamlRequestFileUploadContext implements RequestContext {
        @Override
        public String getCharacterEncoding() {
            return charset(contentType.toString());
        }

        @Override
        public String getContentType() {
            return contentType.toString();
        }

        @Override
        public int getContentLength() {
            return content.length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
    }
}
