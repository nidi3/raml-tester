package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.RamlResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 *
 */
public class ServletRamlResponse extends HttpServletResponseWrapper implements RamlResponse {
    private String characterEncoding = "iso-8859-1";
    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    private PrintWriter writer;
    private ServletOutputStream outputStream;

    private int status = HttpServletResponse.SC_OK;

    public ServletRamlResponse(HttpServletResponse delegate) {
        super(delegate);
    }

    @Override
    public void setStatus(int sc) {
        status = sc;
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        status = sc;
        super.setStatus(sc, sm);
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
    public String getContentAsString() {
        try {
            flushBuffer();
            return characterEncoding != null ? content.toString(characterEncoding) : content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Problem getting content", e);
        }
    }

    @Override
    public int getStatus() {
        return status;
    }
}
