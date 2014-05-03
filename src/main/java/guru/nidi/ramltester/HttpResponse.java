package guru.nidi.ramltester;

/**
 *
 */
public interface HttpResponse {
    int getStatus();

    String getContentType();

    String getContentAsString();
}
