package guru.nidi.ramltester;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class RamlLoaderTest {
    @Test
    public void fromApiPortal() throws IOException {
        final RamlRepository repository = RamlLoaders.loadFromApiPortal(System.getenv("API_PORTAL_USER"), System.getenv("API_PORTAL_PASS"));
        final RamlDefinition definition = repository.getRaml("test.raml");
        Assert.assertNotNull(definition);
    }
}
