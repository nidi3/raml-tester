package guru.nidi.ramltester.loader;

import java.io.InputStream;

/**
 *
 */
public interface RamlResourceLoader {
    InputStream fetchResource(String name);
}
