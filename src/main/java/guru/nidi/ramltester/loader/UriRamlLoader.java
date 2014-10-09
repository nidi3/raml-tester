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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles resources with absolute URIs. Handling of relative URIs are delegated to another RamlLoader.
 * Loaders are registered in META-INF/services/guru.nidi.ramltester.loader.RamlLoaderFactory
 */
public class UriRamlLoader implements RamlLoader {
    private static final Pattern ABSOLUTE_URI_PATTERN = Pattern.compile("([^:]+)://(.+)/([^/]+)");

    private static Map<String, RamlLoaderFactory> factories = new HashMap<>();

    static {
        final ServiceLoader<RamlLoaderFactory> loader = ServiceLoader.load(RamlLoaderFactory.class);
        for (Iterator<RamlLoaderFactory> iter = loader.iterator(); iter.hasNext(); ) {
            final RamlLoaderFactory factory = iter.next();
            factories.put(factory.supportedProtocol(), factory);
        }
    }

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
        final int protocol = secondProtocol < 0 ? firstProtocol : secondProtocol;
        final int endOfFirst = name.lastIndexOf("/", protocol);
        if (endOfFirst >= 0) {
            return name.substring(endOfFirst + 1);
        }
        return name;
    }

    private RamlLoader absoluteLoader(String protocol, String base) {
        final RamlLoaderFactory factory = factories.get(protocol);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported protocol " + protocol);
        }
        return factory.getRamlLoader(base);
    }
}
