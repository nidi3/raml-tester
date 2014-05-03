package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.HttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class SpringMockHttpResponse implements HttpResponse {
    private final MockHttpServletResponse delegate;

    public SpringMockHttpResponse(MockHttpServletResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getStatus() {
        return delegate.getStatus();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContentAsString() {
        try {
            return delegate.getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Problem extracting response content", e);
        }
    }
}
