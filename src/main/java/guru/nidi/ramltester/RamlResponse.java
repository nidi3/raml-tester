package guru.nidi.ramltester;

/**
 *
 */
public interface RamlResponse {
    int getStatus();

    String getContentType();

    String getContentAsString();
}
