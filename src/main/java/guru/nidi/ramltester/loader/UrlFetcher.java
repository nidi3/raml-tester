package guru.nidi.ramltester.loader;

import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public interface UrlFetcher {
    InputStream fetchFromUrl(CloseableHttpClient client, String base, String name) throws IOException;
}
