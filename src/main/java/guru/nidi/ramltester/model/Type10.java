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
package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class Type10 implements UnifiedType {
    private final TypeDeclaration type;

    public Type10(TypeDeclaration type) {
        this.type = type;
    }

    static List<UnifiedType> of(List<TypeDeclaration> types) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final TypeDeclaration td : types) {
            res.add(new Type10(td));
        }
        return res;
    }

    @Override
    public TypeDeclaration getDelegate() {
        return type;
    }

    @Override
    public String name() {
        return type.name();
    }

    @Override
    public String description() {
        return type.description() == null ? null : type.description().value();
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

    @Override
    public String defaultValue() {
        return type.defaultValue();
    }

    @Override
    public boolean required() {
        return type.required();
    }

    @Override
    public boolean repeat() {
        return type instanceof ArrayTypeDeclaration;
    }
}
