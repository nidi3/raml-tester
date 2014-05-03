package guru.nidi.ramltester;

import org.raml.model.Raml;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

/**
 *
 */
public class RamlLoaders {
    public static Raml fromClasspath(String name) {
        return new RamlDocumentBuilder(new ClassPathResourceLoader()).build(name);
    }

    public static Raml fromClasspath(Class<?> basePackage, String name) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/') + "/" + name);
    }
}
