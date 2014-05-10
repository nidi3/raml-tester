package guru.nidi.ramltester.apiportal;

import org.junit.Before;
import org.junit.Test;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class ApiPortalLoaderTest {
    private ApiPortalLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = new ApiPortalLoader(System.getenv("API_PORTAL_USER"), System.getenv("API_PORTAL_PASS"));
    }

    @Test
    public void fromApiPortalOk() throws IOException {
        assertNotNull(new RamlDocumentBuilder(loader).build("test.raml"));
    }

    @Test(expected = NullPointerException.class)
    public void fromApiPortalUnknownFile() throws IOException {
        new RamlDocumentBuilder(loader).build("huhuhuhuhu.raml");
    }

    @Test(expected = IOException.class)
    public void fromApiPortalUnknownUser() throws IOException {
        new RamlDocumentBuilder(new ApiPortalLoader("wwwwww", "blalbulbi")).build("test.raml");
    }
}
