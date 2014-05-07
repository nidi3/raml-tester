package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.RamlDefinition;

/**
 *
 */
public class RequestResponseMatchers {
    private final String servletUri;

    RequestResponseMatchers(String servletUri) {
        this.servletUri = servletUri;
    }

    public RequestResponseMatchers withServletUri(String servletUri) {
        return new RequestResponseMatchers(servletUri);
    }

    public RamlMatcher matchesRaml(RamlDefinition ramlDefinition) {
        return new RamlMatcher(ramlDefinition, servletUri);
    }

}
