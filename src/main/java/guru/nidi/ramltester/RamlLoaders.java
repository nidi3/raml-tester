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

import guru.nidi.ramltester.apiportal.ApiPortalLoader;
import guru.nidi.ramltester.core.RestassuredSchemaValidator;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.RamlParserResourceLoader;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.IOException;

/**
 *
 */
public class RamlLoaders {
    private final String name;
    private final ResourceLoader resourceLoader;
    private final SchemaValidator schemaValidator;

    RamlLoaders(String name, ResourceLoader resourceLoader, SchemaValidator schemaValidator) {
        this.name = name;
        this.resourceLoader = resourceLoader;
        this.schemaValidator = schemaValidator != null ? schemaValidator : new RestassuredSchemaValidator();
    }

    public RamlLoaders withResourceLoader(ResourceLoader resourceLoader) {
        return new RamlLoaders(name, resourceLoader, schemaValidator);
    }

    public RamlLoaders withSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlLoaders(name, resourceLoader, schemaValidator);
    }

    public RamlDefinition fromClasspath(String basePackage) {
        final SchemaValidator validator = schemaValidator.withResourceLoader(Thread.currentThread().getContextClassLoader().getResource(basePackage).toString(), null);
        return new RamlDefinition(documentBuilder(new ClassPathResourceLoader()).build(basePackage + "/" + name), validator);
    }

    public RamlDefinition fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public RamlDefinition fromApiPortal(ApiPortalLoader loader) throws IOException {
        final SchemaValidator validator = schemaValidator.withResourceLoader("apiPortal", loader);
        return new RamlDefinition(documentBuilder(new RamlParserResourceLoader(loader)).build(name), validator);
    }

    public RamlDefinition fromApiPortal(String user, String password) throws IOException {
        return fromApiPortal(new ApiPortalLoader(user, password));
    }

    private RamlDocumentBuilder documentBuilder(ResourceLoader loader) {
        return new RamlDocumentBuilder(resourceLoader == null ? loader : new CompositeResourceLoader(resourceLoader, loader));
    }
}
