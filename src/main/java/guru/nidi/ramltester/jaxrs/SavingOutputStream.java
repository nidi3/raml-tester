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
package guru.nidi.ramltester.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SavingOutputStream extends OutputStream {
    private final OutputStream delegate;
    private final ByteArrayOutputStream saved;

    public SavingOutputStream(OutputStream delegate) {
        this.delegate = delegate;
        this.saved = new ByteArrayOutputStream();
    }

    public byte[] getSaved() {
        return saved.toByteArray();
    }

    @Override
    public void write(int b) throws IOException {
        IOException e1 = null;
        try {
            delegate.write(b);
        } catch (IOException e) {
            e1 = e;
        }
        saved.write(b);
        throwIfNeeded(e1, null);
    }

    @Override
    public void write(byte[] b) throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate.write(b);
        } catch (IOException e) {
            e1 = e;
        }
        try {
            saved.write(b);
        } catch (IOException e) {
            e2 = e;
        }
        throwIfNeeded(e1, e2);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        IOException e1 = null;
        try {
            delegate.write(b, off, len);
        } catch (IOException e) {
            e1 = e;
        }
        saved.write(b, off, len);
        throwIfNeeded(e1, null);
    }

    @Override
    public void flush() throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate.flush();
        } catch (IOException e) {
            e1 = e;
        }
        try {
            saved.flush();
        } catch (IOException e) {
            e2 = e;
        }
        throwIfNeeded(e1, e2);
    }

    @Override
    public void close() throws IOException {
        IOException e1 = null, e2 = null;
        try {
            delegate.close();
        } catch (IOException e) {
            e1 = e;
        }
        try {
            saved.close();
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
