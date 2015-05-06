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
package guru.nidi.ramltester.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class ClassPathRamlLoader implements RamlLoader {
    private static final String FILE_COLON = "file:";
    private final String base;

    public ClassPathRamlLoader() {
        this("");
    }

    public ClassPathRamlLoader(String base) {
        this.base = (base == null || base.length() == 0) ? "" : base.endsWith("/") ? base : base + "/";
    }

    @Override
    public InputStream fetchResource(String name, long ifModifiedSince) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(base + name);
        if (url == null) {
            throw new ResourceNotFoundException(name);
        }
        try {
            final String path = url.getPath();
            switch (url.getProtocol()) {
                case "file":
                    final File file = new File(path);
                    return file.lastModified() > ifModifiedSince ? url.openStream() : null;
                case "jar":
                    if (path.startsWith(FILE_COLON)) {
                        final int pos = path.indexOf('!');
                        final File jar = new File(path.substring(FILE_COLON.length(), pos));
                        return jar.lastModified() > ifModifiedSince ? url.openStream() : null;
                    }
                    return url.openStream();
                default:
                    return url.openStream();
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException(name, e);
        }
    }

    @Override
    public String config() {
        return "classpath-" + base;
    }

    public static class Factory implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "classpath";
        }

        @Override
        public RamlLoader getRamlLoader(String base, String username, String password) {
            return new ClassPathRamlLoader(base);
        }
    }

}
