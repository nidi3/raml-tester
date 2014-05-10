package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlRequest;
import guru.nidi.ramltester.util.UriComponents;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

/**
 *
 */
public class SpringMockRamlRequest implements RamlRequest {
    private final MockHttpServletRequest delegate;

    public SpringMockRamlRequest(String servletUri, MockHttpServletRequest delegate) {
        if (servletUri != null) {
            final UriComponents uri = UriComponents.fromHttpUrl(servletUri);
            final String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                throw new IllegalArgumentException("Servlet URI must start with http(s)://");
            }
            delegate.setScheme(scheme);
            delegate.setServerName(uri.getHost());
            if (uri.getPort() != null) {
                delegate.setServerPort(uri.getPort());
            }
            delegate.setContextPath(uri.getPath());
        }
        this.delegate = delegate;
    }

    @Override
    public String getRequestUrl() {
        final StringBuffer requestURL = delegate.getRequestURL();
        final int pathStart = requestURL.length() - delegate.getRequestURI().length();
        return requestURL.substring(0, pathStart) + delegate.getContextPath() + requestURL.substring(pathStart);
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return delegate.getParameterMap();
    }
}
