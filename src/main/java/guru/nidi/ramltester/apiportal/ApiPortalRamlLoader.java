package guru.nidi.ramltester.apiportal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramltester.RamlRepository;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ApiPortalRamlLoader {
    private final String user;
    private final String password;

    private final ObjectMapper mapper;

    public ApiPortalRamlLoader(String user, String password) {
        this.user = user;
        this.password = password;

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public RamlRepository load() throws IOException {
        try(final CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final HttpPost login = new HttpPost("http://api-portal.anypoint.mulesoft.com/ajax/apihub/login-register/form?section=login");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("name", user));
            params.add(new BasicNameValuePair("pass", password));
            params.add(new BasicNameValuePair("form_id", "user_login"));
            login.setEntity(new UrlEncodedFormEntity(params));
            final CloseableHttpResponse loginResponse = client.execute(login);
            if (loginResponse.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new IOException("Login into api portal not successful: " + loginResponse.getStatusLine().getReasonPhrase());
            }
            final HttpGet files = new HttpGet("http://api-portal.anypoint.mulesoft.com/rest/raml/v1/files");
            final CloseableHttpResponse filesResponse = client.execute(files);
            if (filesResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Could not get list of files: " + filesResponse.getStatusLine().getReasonPhrase());
            }
            //final Map map = mapper.readValue(filesResponse.getEntity().getContent(), Map.class);
            final ApiPortalFilesResponse apiPortalFilesResponse = mapper.readValue(filesResponse.getEntity().getContent(), ApiPortalFilesResponse.class);
            return new ApiPortalRamlRepository(apiPortalFilesResponse, null);
        }
    }
}
