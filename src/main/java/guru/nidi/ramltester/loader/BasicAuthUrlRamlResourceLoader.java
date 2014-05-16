package guru.nidi.ramltester.loader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.nio.charset.Charset;

/**
 *
 */
public class BasicAuthUrlRamlResourceLoader extends UrlRamlResourceLoader {
    private final String auth;

    public BasicAuthUrlRamlResourceLoader(String baseUrl, String username, String password, CloseableHttpClient httpClient) {
        super(baseUrl, httpClient);
        auth = encode(username, password);
    }

    public BasicAuthUrlRamlResourceLoader(String baseUrl, String username, String password) {
        this(baseUrl, username, password, null);
    }

    private String encode(String username, String password) {
        return Base64.encodeBase64String(("Basic " + username + ":" + password).getBytes(Charset.forName("iso-8859-1")));
    }

    @Override
    protected HttpGet postProcessGet(HttpGet get) {
        get.addHeader("Authorization", auth);
        return get;
    }
}
