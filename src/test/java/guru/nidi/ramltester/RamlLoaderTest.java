package guru.nidi.ramltester;

import guru.nidi.ramltester.apiportal.ApiPortalLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class RamlLoaderTest {
    private ApiPortalLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = new ApiPortalLoader(System.getenv("API_PORTAL_USER"), System.getenv("API_PORTAL_PASS"));
    }

    @Test
    public void fromApiPortalOk() throws IOException {
        final RamlDefinition ramlDefinition = TestRaml.load("test.raml").fromApiPortal(loader);
        assertNotNull(ramlDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void fromApiPortalUnknownFile() throws IOException {
        TestRaml.load("huhuhuhuhu.raml").fromApiPortal(loader);
    }

    @Test(expected = IOException.class)
    public void fromApiPortalUnknownUser() throws IOException {
        TestRaml.load("test.raml").fromApiPortal("wwwwww", "blalbulbi");
    }
}
