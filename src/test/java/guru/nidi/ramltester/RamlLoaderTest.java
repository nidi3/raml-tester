package guru.nidi.ramltester;

import guru.nidi.ramltester.apiportal.ApiPortalLoader;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class RamlLoaderTest {
    @Test
    public void fromApiPortalOk() throws IOException {
        final RamlDefinition ramlDefinition = TestRaml.load("test.raml").fromApiPortal(System.getenv("API_PORTAL_USER"), System.getenv("API_PORTAL_PASS"));
        assertNotNull(ramlDefinition);
    }

    @Test(expected = IOException.class)
    public void fromApiPortalUnknownUser() throws IOException {
        final ApiPortalLoader loader = new ApiPortalLoader("wwwwww", "blalbulbi");
        TestRaml.load("test.raml").fromApiPortal(loader);
    }
}
