package guru.nidi.ramltester;

import java.util.Map;

/**
 *
 */
public interface HttpRequest {
    String getRequestUrl();

    String getMethod();

    Map<String,String[]> getParameterMap();
}
