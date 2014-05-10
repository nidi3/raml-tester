package guru.nidi.ramltester;

import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import guru.nidi.ramltester.spring.SpringMockRamlRequest;
import guru.nidi.ramltester.spring.SpringMockRamlResponse;
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

    public RamlViolationReport testAgainst(RamlRequest request, RamlResponse response) {
        final RamlTester runner = new RamlTester(raml, schemaValidator);
        runner.test(request, response);
        return runner.getReport();
    }

    public RamlViolationReport testAgainst(MvcResult mvcResult, String servletUri) {
        return testAgainst(
                new SpringMockRamlRequest(servletUri, mvcResult.getRequest()),
                new SpringMockRamlResponse(mvcResult.getResponse()));
    }

    public RamlViolationReport testAgainst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletRamlRequest httpRequest = new ServletRamlRequest((HttpServletRequest) request);
        final ServletRamlResponse httpResponse = new ServletRamlResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return testAgainst(httpRequest, httpResponse);
    }

}

