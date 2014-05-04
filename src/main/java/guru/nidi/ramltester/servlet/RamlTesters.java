package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.RamlTester;
import guru.nidi.ramltester.RamlViolations;
import org.raml.model.Raml;

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
    public static RamlViolations executeFilterChain(Raml raml, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletHttpRequest httpRequest = new ServletHttpRequest((HttpServletRequest) request);
        final ServletHttpResponse httpResponse = new ServletHttpResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return new RamlTester().test(raml, httpRequest, httpResponse);
    }
}
