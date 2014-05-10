package guru.nidi.ramltester.core;

/**
 *
 */
public interface RamlResponse {
    int getStatus();

    String getContentType();

    String getContentAsString();
}
