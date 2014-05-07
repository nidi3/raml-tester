package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlTester;
import guru.nidi.ramltester.RamlViolations;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class RamlTesters {
    public static RamlViolations executeFilterChain(RamlDefinition ramlDefinition, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletHttpRequest httpRequest = new ServletHttpRequest((HttpServletRequest) request);
        final ServletHttpResponse httpResponse = new ServletHttpResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return new RamlTester().test(ramlDefinition, httpRequest, httpResponse);
    }
}
