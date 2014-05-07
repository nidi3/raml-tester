package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class ServletHttpRequest extends HttpServletRequestWrapper implements HttpRequest {
    public ServletHttpRequest(HttpServletRequest delegate) {
        super(delegate);
    }

    private HttpServletRequest request() {
        return (HttpServletRequest) getRequest();
    }

    @Override
    public String getRequestUrl() {
        return request().getRequestURL().toString();
    }
}
