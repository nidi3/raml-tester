package guru.nidi.ramltester;

import guru.nidi.ramltester.servlet.ServletHttpRequest;
import guru.nidi.ramltester.servlet.ServletHttpResponse;
import guru.nidi.ramltester.spring.SpringMockHttpRequest;
import guru.nidi.ramltester.spring.SpringMockHttpResponse;
import org.raml.model.Raml;
import org.springframework.test.web.servlet.MvcResult;

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
public class RamlDefinition {
    private final Raml raml;
    private final SchemaValidator schemaValidator;

    public RamlDefinition(Raml raml, SchemaValidator schemaValidator) {
        this.raml = raml;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public RamlDefinition withSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlDefinition(raml, schemaValidator);
    }

    public RamlViolations testAgainst(HttpRequest request, HttpResponse response) {
        final RamlTester runner = new RamlTester(raml, schemaValidator);
        runner.test(request, response);
        return runner.getViolations();
    }

    public RamlViolations testAgainst(MvcResult mvcResult, String servletUri) {
        return testAgainst(
                new SpringMockHttpRequest(servletUri, mvcResult.getRequest()),
                new SpringMockHttpResponse(mvcResult.getResponse()));
    }

    public RamlViolations testAgainst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletHttpRequest httpRequest = new ServletHttpRequest((HttpServletRequest) request);
        final ServletHttpResponse httpResponse = new ServletHttpResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return testAgainst(httpRequest, httpResponse);
    }

}

