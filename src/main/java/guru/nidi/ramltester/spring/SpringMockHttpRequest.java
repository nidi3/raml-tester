package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.HttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

/**
 *
 */
public class SpringMockHttpRequest implements HttpRequest {
    private final MockHttpServletRequest delegate;

    public SpringMockHttpRequest(MockHttpServletRequest delegate) {
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
