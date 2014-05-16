package guru.nidi.ramltester.loader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class LoaderTest {
    @Test
    public void classPathOk() throws IOException {
        final InputStream stream = new ClassPathRamlResourceLoader("guru/nidi/ramltester").fetchResource("simple.raml");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlResourceLoader.ResourceNotFoundException.class)
    public void classPathNok() {
        new ClassPathRamlResourceLoader("guru/nidi/ramltester").fetchResource("bla");
    }

    @Test
    public void fileOk() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        final InputStream stream = new FileRamlResourceLoader(new File(resource.getPath().toString())).fetchResource("simple.raml");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlResourceLoader.ResourceNotFoundException.class)
    public void fileNok() {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        new FileRamlResourceLoader(new File(resource.getPath().toString())).fetchResource("bla");
    }

    @Test
    public void urlOk() throws IOException {
        final InputStream stream = new UrlRamlResourceLoader("http://en.wikipedia.org/wiki").fetchResource("Short");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlResourceLoader.ResourceNotFoundException.class)
    public void urlNok() {
        new UrlRamlResourceLoader("http://en.wikipedia.org").fetchResource("dfkjsdfhfs");
    }

}
