package guru.nidi.ramltester;

import guru.nidi.ramltester.apiportal.ApiPortalRamlLoader;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.IOException;

/**
 *
 */
public class RamlLoaders {
    public static RamlDefinition loadFromClasspath(String name) {
        return new RamlDefinition(new RamlDocumentBuilder(new ClassPathResourceLoader()).build(name), null);
    }

    public static RamlDefinition loadFromClasspath(Class<?> basePackage, String name) {
        return loadFromClasspath(basePackage.getPackage().getName().replace('.', '/') + "/" + name);
    }

    public static RamlRepository loadFromApiPortal(String user, String password) throws IOException {
        return new ApiPortalRamlLoader(user, password).load();
    }

}
