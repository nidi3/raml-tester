package guru.nidi.ramltester.loader;

import com.github.fge.jsonschema.core.load.download.URIDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 *
 */
public class UriDownloaderResourceLoader implements URIDownloader {
    private final RamlResourceLoader delegate;

    public UriDownloaderResourceLoader(RamlResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream fetch(URI source) throws IOException {
        return delegate.fetchResource(source.getPath());
    }
}
