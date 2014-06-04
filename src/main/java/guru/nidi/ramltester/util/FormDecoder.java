package guru.nidi.ramltester.util;

import guru.nidi.ramltester.core.MediaType;
import guru.nidi.ramltester.core.RamlRequest;
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


    public Values decode(RamlRequest request) {
        if (request.getContentType() == null) {
            return new Values();
        }
        final MediaType type = MediaType.valueOf(request.getContentType());
        if (type.isCompatibleWith(URL_ENCODED)) {
            final String charset = type.getCharset(DEFAULT_CHARSET);
            try {
                final String content = IoUtils.readIntoString(new InputStreamReader(new ByteArrayInputStream(request.getContent()), charset));
                return decodeUrlEncoded(content, charset);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unknown charset " + charset);
            }
        }
        if (type.isCompatibleWith(MULTIPART)) {
            return decodeMultipart(request);
        }
        return new Values();
    }

    private static String charset(String contentType) {
        return contentType == null ? DEFAULT_CHARSET : MediaType.valueOf(contentType).getCharset(DEFAULT_CHARSET);
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
        Values q = new Values();
        if (content != null) {
            Matcher m = QUERY_PARAM.matcher(content);
            while (m.find()) {
                String name = urlDecode(m.group(1), charset);
                String eq = m.group(2);
                String value = m.group(3);
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
