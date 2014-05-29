package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.util.Values;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

/**
 *
 */
class SpringUtils {
    private SpringUtils() {
    }

    static String contentTypeOf(HttpHeaders headers) {
        final MediaType contentType = headers.getContentType();
        return contentType == null ? null : contentType.toString();
    }

    static Values headerValuesOf(HttpHeaders headers) {
        final Values values = new Values();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            values.addValues(entry.getKey(), entry.getValue());
        }
        return values;
    }
}
