package guru.nidi.ramltester.httpcomponents;

import guru.nidi.ramltester.util.Values;
import org.apache.http.*;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 *
 */
class HttpComponentsUtils {
    private HttpComponentsUtils() {
    }

    static String encodingOf(HttpEntity entity) {
        return entity.getContentEncoding() != null ? entity.getContentEncoding().getValue() : "utf-8";
    }

    static String contentTypeOf(HttpMessage message) {
        final Header contentType = message.getFirstHeader("Content-Type");
        return contentType == null ? null : contentType.getValue();
    }

    static Values headerValuesOf(HttpMessage message) {
        Values headers = new Values();
        for (Header header : message.getAllHeaders()) {
            headers.addValue(header.getName(), header.getValue());
        }
        return headers;
    }

    static BufferedHttpEntity buffered(HttpEntity entity) {
        try {
            return new BufferedHttpEntity(entity);
        } catch (IOException e) {
            throw new RuntimeException("Could not read content of entity", e);
        }
    }

    static HttpResponse buffered(HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if (!entity.isRepeatable()) {
            response.setEntity(buffered(entity));
        }
        return response;
    }

    static HttpEntityEnclosingRequest buffered(HttpEntityEnclosingRequest request) {
        final HttpEntity entity = request.getEntity();
        if (!entity.isRepeatable()) {
            request.setEntity(buffered(entity));
        }
        return request;
    }

    static String contentOf(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, encodingOf(entity));
        } catch (IOException e) {
            throw new RuntimeException("Could not get response content", e);
        }
    }
}
