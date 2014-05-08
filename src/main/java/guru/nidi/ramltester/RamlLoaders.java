package guru.nidi.ramltester;

import guru.nidi.ramltester.apiportal.ApiPortalLoader;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.IOException;

/**
 *
 */
public class RamlLoaders {
    private final String name;
    private final ResourceLoader resourceLoader;

    RamlLoaders(String name, ResourceLoader resourceLoader) {
        this.name = name;
        this.resourceLoader = resourceLoader;
    }

    public RamlLoaders withResourceLoader(ResourceLoader resourceLoader) {
        return new RamlLoaders(name, resourceLoader);
    }

    public RamlDefinition fromClasspath(String basePackage) {
        return new RamlDefinition(documentBuilder(new ClassPathResourceLoader()).build(basePackage + "/" + name), null);
    }

    public RamlDefinition fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public RamlDefinition fromApiPortal(ApiPortalLoader loader) throws IOException {
        return new RamlDefinition(documentBuilder(loader).build(name), null);
    }

    public RamlDefinition fromApiPortal(String user, String password) throws IOException {
        return fromApiPortal(new ApiPortalLoader(user, password));
    }

    private RamlDocumentBuilder documentBuilder(ResourceLoader loader) {
        return new RamlDocumentBuilder(resourceLoader == null ? loader : new CompositeResourceLoader(resourceLoader, loader));
    }
}
