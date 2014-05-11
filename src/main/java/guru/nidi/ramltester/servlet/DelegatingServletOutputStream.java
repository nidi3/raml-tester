/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.servlet;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
class DelegatingServletOutputStream extends ServletOutputStream {
    private final OutputStream delegate1;
    private final OutputStream delegate2;

    public DelegatingServletOutputStream(OutputStream delegate1, OutputStream delegate2) {
        this.delegate1 = delegate1;
        this.delegate2 = delegate2;
    }

    @Override
    public void write(int b) throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate1.write(b);
        } catch (IOException e) {
            e1 = e;
        }
        try {
            delegate2.write(b);
        } catch (IOException e) {
            e2 = e;
        }
        throwIfNeeded(e1, e2);
    }

    @Override
    public void flush() throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate1.flush();
        } catch (IOException e) {
            e1 = e;
        }
        try {
            delegate2.flush();
        } catch (IOException e) {
            e2 = e;
        }
        throwIfNeeded(e1, e2);
    }

    @Override
    public void close() throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate1.close();
        } catch (IOException e) {
            e1 = e;
        }
        try {
            delegate2.close();
        } catch (IOException e) {
            e2 = e;
        }
        throwIfNeeded(e1, e2);
    }

    private void throwIfNeeded(IOException e1, IOException e2) throws IOException {
        if (e1 != null) {
            throw e1;
        }
        if (e2 != null) {
            throw e2;
        }
    }
}
