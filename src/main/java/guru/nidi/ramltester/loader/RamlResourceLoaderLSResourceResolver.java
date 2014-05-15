package guru.nidi.ramltester.loader;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
*
*/
public class RamlResourceLoaderLSResourceResolver implements LSResourceResolver {
    private static final DOMImplementationLS DOM_IMPLEMENTATION_LS;

    static {
        try {
            DOM_IMPLEMENTATION_LS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not initialize DOM implementation", e);
        }
    }

    private final RamlResourceLoader resourceLoader;

    public RamlResourceLoaderLSResourceResolver(RamlResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        final LSInput input = DOM_IMPLEMENTATION_LS.createLSInput();
        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setByteStream(resourceLoader.fetchResource(systemId));
        return input;
    }
}
