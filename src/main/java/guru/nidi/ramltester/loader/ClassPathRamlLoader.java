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
            switch (url.getProtocol()) {
                case "file":
                    final File file = new File(url.getPath());
                    return file.lastModified() > ifModifiedSince ? url.openStream() : null;
                case "jar":
                    if (url.getPath().startsWith("file:")) {
                        final int pos = url.getPath().indexOf('!');
                        final File jar = new File(url.getPath().substring(5, pos));
                        return jar.lastModified() > ifModifiedSince ? url.openStream() : null;
                    }
                    return url.openStream();
                default:
                    return url.openStream();
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException(name);
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
