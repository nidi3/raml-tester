package guru.nidi.ramltester.servlet;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
class DelegatingServletInputStream extends ServletInputStream {
    private final InputStream delegate;

    public DelegatingServletInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        delegate.close();
    }
}
