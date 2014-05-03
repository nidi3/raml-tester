package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *
 */
public class ServletHttpRequest implements HttpRequest {
    private final HttpServletRequest delegate;

    public ServletHttpRequest(HttpServletRequest delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getRequestURI() {
        return delegate.getRequestURI();
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
