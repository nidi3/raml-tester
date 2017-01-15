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
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.ExternalTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.internal.impl.v10.type.TypeId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Body10 implements RamlBody {
    private final TypeDeclaration type;

    Body10(TypeDeclaration type) {
        this.type = type;
    }

    static List<RamlBody> of(List<TypeDeclaration> bodies) {
        final List<RamlBody> res = new ArrayList<>();
        for (final TypeDeclaration t : bodies) {
            res.add(new Body10(t));
        }
        return res;
    }

    @Override
    public String name() {
        return type.name();
    }

    @Override
    public List<RamlType> formParameters() {
        return Collections.emptyList();
    }

    @Override
    public String type() {
        return TypeId.ANY.getType().equals(type.type()) ? null : type.type();
    }

    @Override
    public String typeDefinition() {
        return type instanceof ExternalTypeDeclaration ? ((ExternalTypeDeclaration) type).schemaContent() : type();
    }

    @Override
    public List<String> examples() {
        final List<String> res = new ArrayList<>();
        for (final ExampleSpec ex : type.examples()) {
            res.add(ex.value());
        }
        if (type.example() != null) {
            res.add(type.example().value());
        }
        return res;
    }
}
