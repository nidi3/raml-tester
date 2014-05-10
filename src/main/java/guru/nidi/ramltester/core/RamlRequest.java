package guru.nidi.ramltester.core;

import java.util.Map;

/**
 *
 */
public interface RamlRequest {
    String getRequestUrl();

    String getMethod();

    Map<String,String[]> getParameterMap();
}
