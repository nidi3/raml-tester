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
package guru.nidi.ramltester;

import guru.nidi.ramltester.apidesigner.ApiRamlLoader;
import guru.nidi.ramltester.loader.ClassPathRamlLoader;
import guru.nidi.ramltester.loader.FileRamlLoader;
import guru.nidi.ramltester.loader.RamlLoader;
import guru.nidi.ramltester.loader.UrlRamlLoader;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RamlTester {
    private static final Pattern URI_PATTERN = Pattern.compile("([^:]+)://(.*)");

    private RamlTester() {
    }

    public static RamlLoaders fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public static RamlLoaders fromClasspath(String basePackage) {
        return usingLoader(new ClassPathRamlLoader(basePackage));
    }

    public static RamlLoaders fromFile(File baseDirectory) {
        return usingLoader(new FileRamlLoader(baseDirectory));
    }

    public static RamlLoaders fromUrl(String baseUrl) {
        return usingLoader(new UrlRamlLoader(baseUrl));
    }

    public static RamlLoaders fromApiPortal(String user, String password) throws IOException {
        return usingLoader(new ApiRamlLoader(user, password));
    }

    public static RamlLoaders fromApiDesigner(String url) throws IOException {
        return usingLoader(new ApiRamlLoader(url));
    }

    public static RamlDefinition loadFromUri(String uri) {
        final Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        final String path = matcher.group(2);
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            throw new IllegalArgumentException("Missing '/' in uri: " + uri);
        }
        if (pos == path.length() - 1) {
            throw new IllegalArgumentException("uri must not end with '/': " + uri);
        }
        String base = path.substring(0, pos);
        String file = path.substring(pos + 1);
        switch (matcher.group(1)) {
            case "classpath":
                return fromClasspath(base).load(file);
            case "file":
                return fromFile(new File(base)).load(file);
            case "http":
            case "https":
                return fromUrl(base).load(file);
            default:
                throw new IllegalArgumentException("Unknown scheme " + matcher.group(1));
        }
    }

    public static RamlLoaders usingLoader(RamlLoader loader) {
        return new RamlLoaders(loader);
    }
}
