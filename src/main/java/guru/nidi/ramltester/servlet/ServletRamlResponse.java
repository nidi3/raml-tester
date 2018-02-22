/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.IoUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

public class ServletRamlResponse extends HttpServletResponseWrapper implements RamlResponse {
    private String characterEncoding = "iso-8859-1";
    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    private PrintWriter writer;
    private ServletOutputStream outputStream;

    private int status = HttpServletResponse.SC_OK;
    private final Values headers = new Values();

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
    public void setHeader(String name, String value) {
        headers.setValue(name, value);
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.addValue(name, value);
        super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.setValue(name, Integer.toString(value));
        super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.addValue(name, Integer.toString(value));
        super.addIntHeader(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.setValue(name, dateToString(date));
        super.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.addValue(name, dateToString(date));
        super.addDateHeader(name, date);
    }

    private String dateToString(long date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(date));
    }

    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
        characterEncoding = charset;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            final Writer targetWriter = characterEncoding == null
                    ? new OutputStreamWriter(content)
                    : new OutputStreamWriter(content, characterEncoding);
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
    public byte[] getContent() {
        try {
            flushBuffer();
            final byte[] data = content.toByteArray();
            return "gzip".equalsIgnoreCase(getHeader("Content-Encoding")) ? gunzip(data) : data;
        } catch (IOException e) {
            throw new RamlCheckerException("Problem getting content", e);
        }
    }

    private byte[] gunzip(byte[] gzipped) throws IOException {
        return IoUtils.readIntoByteArray(new GZIPInputStream(new ByteArrayInputStream(gzipped)));
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Values getHeaderValues() {
        return headers;
    }
}
