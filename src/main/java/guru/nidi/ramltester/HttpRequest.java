package guru.nidi.ramltester;

import java.util.Map;

/**
 *
 */
public interface HttpRequest {
    String getRequestURI();

    String getMethod();

    Map<String,String[]> getParameterMap();
}
