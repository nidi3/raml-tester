package guru.nidi.ramltester.loader;

import java.io.InputStream;

/**
 *
 */
public class ClassPathRamlResourceLoader implements RamlResourceLoader {
    private final String base;

    public ClassPathRamlResourceLoader(String base) {
        this.base = base;
    }

    @Override
    public InputStream fetchResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(base + "/" + name);
    }
}
