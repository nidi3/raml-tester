package guru.nidi.ramltester.loader;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FormLoginUrlRamlResourceLoader extends UrlRamlResourceLoader {
    private final String loginUrl;
    private final String login;
    private final String password;
    private final String loginField;
    private final String passwordField;

    public FormLoginUrlRamlResourceLoader(String baseUrl, String loadPath, String loginPath, String login, String password, String loginField, String passwordField, CloseableHttpClient httpClient) {
        super(baseUrl + "/" + loadPath, httpClient);
        this.login = login;
        this.password = password;
        this.loginUrl = baseUrl + "/" + loginPath;
        this.loginField = loginField;
        this.passwordField = passwordField;
    }

    public FormLoginUrlRamlResourceLoader(String baseUrl, String loadPath, String loginPath, String login, String password, String loginField, String passwordField) {
        this(baseUrl, loadPath, loginPath, login, password, loginField, passwordField, null);
    }

    @Override
    public InputStream fetchResource(String name) {
        try {
            final HttpPost login = new HttpPost(loginUrl);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(loginField, this.login));
            params.add(new BasicNameValuePair(passwordField, password));
            postProcessLoginParameters(params);
            login.setEntity(new UrlEncodedFormEntity(params));
            final CloseableHttpResponse getResult = client.execute(postProcessLogin(login));
            if (getResult.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new ResourceNotFoundException(name, "Could not login: " + getResult.getStatusLine().toString());
            }
            EntityUtils.consume(getResult.getEntity());
            return super.fetchResource(name);
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    protected void postProcessLoginParameters(List<NameValuePair> parameters) {
    }

    protected HttpPost postProcessLogin(HttpPost login) {
        return login;
    }
}
