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

import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.CompositeRamlLoader;
import guru.nidi.ramltester.loader.RamlLoader;
import guru.nidi.ramltester.loader.RamlLoaderRamlParserResourceLoader;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

/**
 *
 */
public class RamlLoaders {
    private final RamlLoader loader;
    private final SchemaValidators schemaValidators;

    public RamlLoaders(RamlLoader loader, SchemaValidators schemaValidators) {
        this.loader = loader;
        this.schemaValidators = schemaValidators;
    }

    public RamlLoaders(RamlLoader loader) {
        this(loader, SchemaValidators.standard());
    }

    public RamlLoaders addSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlLoaders(loader, schemaValidators.addSchemaValidator(schemaValidator));
    }

    public RamlLoaders addLoader(RamlLoader loader) {
        return new RamlLoaders(new CompositeRamlLoader(this.loader, loader), schemaValidators);
    }

    public RamlDefinition load(String name) {
        final Raml raml = new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(loader)).build(name);
        final SchemaValidators validators = schemaValidators.withResourceLoader(loader);
        return new RamlDefinition(raml, validators);
    }

}
