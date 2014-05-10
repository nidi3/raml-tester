package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.RamlRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class ServletRamlRequest extends HttpServletRequestWrapper implements RamlRequest {
    public ServletRamlRequest(HttpServletRequest delegate) {
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
