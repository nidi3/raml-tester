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

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 */
public class RamlLoaderLSResourceResolver implements LSResourceResolver {
    private static final DOMImplementationLS DOM_IMPLEMENTATION_LS;

    static {
        try {
            DOM_IMPLEMENTATION_LS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not initialize DOM implementation", e);
        }
    }

    private final RamlLoader resourceLoader;

    public RamlLoaderLSResourceResolver(RamlLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        final LSInput input = DOM_IMPLEMENTATION_LS.createLSInput();
        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setByteStream(resourceLoader.fetchResource(systemId, -1));
        return input;
    }
}
