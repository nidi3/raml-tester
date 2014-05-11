package guru.nidi.ramltester;

import guru.nidi.ramltester.apiportal.ApiPortalLoader;
import guru.nidi.ramltester.core.RestassuredSchemaValidator;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.RamlParserResourceLoader;
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
    private final SchemaValidator schemaValidator;

    RamlLoaders(String name, ResourceLoader resourceLoader, SchemaValidator schemaValidator) {
        this.name = name;
        this.resourceLoader = resourceLoader;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public RamlLoaders withResourceLoader(ResourceLoader resourceLoader) {
        return new RamlLoaders(name, resourceLoader, schemaValidator);
    }

    public RamlLoaders withSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlLoaders(name, resourceLoader, schemaValidator);
    }

    public RamlDefinition fromClasspath(String basePackage) {
        final SchemaValidator validator = schemaValidator.withResourceLoader(Thread.currentThread().getContextClassLoader().getResource(basePackage).toString(), null);
        return new RamlDefinition(documentBuilder(new ClassPathResourceLoader()).build(basePackage + "/" + name), validator);
    }

    public RamlDefinition fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public RamlDefinition fromApiPortal(ApiPortalLoader loader) throws IOException {
        final SchemaValidator validator = schemaValidator.withResourceLoader("apiPortal", loader);
        return new RamlDefinition(documentBuilder(new RamlParserResourceLoader(loader)).build(name), validator);
    }

    public RamlDefinition fromApiPortal(String user, String password) throws IOException {
        return fromApiPortal(new ApiPortalLoader(user, password));
    }

    private RamlDocumentBuilder documentBuilder(ResourceLoader loader) {
        return new RamlDocumentBuilder(resourceLoader == null ? loader : new CompositeResourceLoader(resourceLoader, loader));
    }
}
