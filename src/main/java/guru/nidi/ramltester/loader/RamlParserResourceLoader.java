package guru.nidi.ramltester.loader;

import org.raml.parser.loader.ResourceLoader;

import java.io.InputStream;

/**
 *
 */
public class RamlParserResourceLoader implements ResourceLoader {
    private final RamlResourceLoader delegate;

    public RamlParserResourceLoader(RamlResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream fetchResource(String resourceName) {
        return delegate.fetchResource(resourceName);
    }
}
