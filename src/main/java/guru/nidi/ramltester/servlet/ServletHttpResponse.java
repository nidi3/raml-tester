package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.HttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 *
 */
public class ServletHttpResponse extends HttpServletResponseWrapper implements HttpResponse {
    private final HttpServletResponse delegate;

    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);
    private String characterEncoding = "iso-8859-1";

    private PrintWriter writer;
    private ServletOutputStream outputStream;

    public ServletHttpResponse(HttpServletResponse delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
        characterEncoding = charset;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            Writer targetWriter = characterEncoding != null ?
                    new OutputStreamWriter(content, characterEncoding) : new OutputStreamWriter(content);
            writer = new PrintWriter(new DelegatingWriter(super.getWriter(), targetWriter));
        }
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new DelegatingServletOutputStream(super.getOutputStream(), content);
        }
        return outputStream;
    }

    @Override
    public int getStatus() {
        return delegate.getStatus();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContentAsString() {
        try {
            flushBuffer();
            return characterEncoding != null ? content.toString(characterEncoding) : content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Problem getting content", e);
        }
    }
}
