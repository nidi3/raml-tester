package guru.nidi.ramltester.loader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class SimpleUrlFetcher implements UrlFetcher {
    @Override
    public InputStream fetchFromUrl(CloseableHttpClient client, String base, String name) throws IOException {
        final HttpGet get = postProcessGet(new HttpGet(base + "/" + encodeUrl(name)));
        final CloseableHttpResponse getResult = client.execute(get);
        if (getResult.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Http response status not ok: " + getResult.getStatusLine().toString());
        }
        return getResult.getEntity().getContent();
    }

    protected String encodeUrl(String name) {
        return name.replace(" ", "%20");
    }

    protected HttpGet postProcessGet(HttpGet get) {
        return get;
    }

}
