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

import guru.nidi.ramltester.apidesigner.ApiRamlLoader;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles resources with absolute URIs. Handling of relative URIs are delegated to another RamlLoader.
 */
public class UriRamlLoader implements RamlLoader {
    private static final Pattern ABSOLUTE_URI_PATTERN = Pattern.compile("([^:]+)://(.+)/([^/]+)");

    private final RamlLoader relativeLoader;

    public UriRamlLoader(RamlLoader relativeLoader) {
        this.relativeLoader = relativeLoader;
    }

    @Override
    public InputStream fetchResource(String name) throws ResourceNotFoundException {
        name = normalizeResourceName(name);
        final Matcher matcher = ABSOLUTE_URI_PATTERN.matcher(name);
        if (matcher.matches()) {
            return absoluteLoader(matcher.group(1), matcher.group(2)).fetchResource(matcher.group(3));
        }
        if (relativeLoader == null) {
            throw new IllegalArgumentException("Expected absolute uri (<protocol>://<base>/<file>), but got '" + name + "'");
        }
        return relativeLoader.fetchResource(name);
    }

    //raml parser does its own absolute/relative handling (org.raml.parser.tagresolver.ContextPath#resolveAbsolutePath)
    // -> hack to undo this
    private String normalizeResourceName(String name) {
        if (name.startsWith("//")) {
            return "classpath:" + name;
        }
        final int firstProtocol = name.indexOf("://");
        final int secondProtocol = name.indexOf("://", firstProtocol + 1);
        if (secondProtocol > 0) {
            final int endOfFirst = name.lastIndexOf("/", secondProtocol);
            return name.substring(endOfFirst + 1);
        }
        return name;
    }

    private RamlLoader absoluteLoader(String protocol, String base) {
        switch (protocol) {
            case "classpath":
                return new ClassPathRamlLoader(base);
            case "file":
                return new FileRamlLoader(new File(base));
            case "http":
            case "https":
                return new UrlRamlLoader(protocol + "://" + base);
            case "apiportal":
                final String[] cred = base.split(":");
                if (cred.length != 2) {
                    throw new IllegalArgumentException("Username and password must be separated by ':'");
                }
                return new ApiRamlLoader(cred[0], cred[1]);
            case "apidesigner":
                return new ApiRamlLoader(base);
            default:
                throw new IllegalArgumentException("Unknown protocol " + protocol);
        }
    }
}
