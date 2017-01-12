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

import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.util.Message;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class Type10 implements UnifiedType {
    private TypeDeclaration type;

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

    private String elementType() {
        final String t = type.type();
        if (t.endsWith("[]")) {
            return t.substring(0, t.length() - 2);
        }
        if (type instanceof ArrayTypeDeclaration) {
            final String items = ((ArrayTypeDeclaration) type).items().type();
            return items.equals("array") ? "string" : items;
        }
        return null;
    }

    @Override
    public void validate(Object payload, RamlViolations violations, Message message) {
        if (payload instanceof Collection) {
            //repeat payload without array type -> just caller's error message
            if (elementType() != null) {
                doValidate(join((Collection<?>) payload, elementType().equals("string") ? "\"" : ""), violations, message);
            }
        } else if (payload == null) {
            if (type.type().equals("string")) {
                doValidate("", violations, message);
            } else {
                violations.add(message.withInnerParam(new Message("value", "empty").withMessageParam("value.empty")));
            }
        } else {
            doValidate(payload.toString(), violations, message);
        }
    }

    private void doValidate(String value, RamlViolations violations, Message message) {
        for (final ValidationResult res : type.validate(value)) {
            violations.add(message.withInnerParam(new Message("value10", value, res.getMessage())));
        }
    }

    private String join(Collection<?> coll, String quote) {
        final StringBuilder sb = new StringBuilder("[");
        for (final Object c : coll) {
            sb.append(quote).append(c.toString()).append(quote).append(",");
        }
        return sb.replace(sb.length() - 1, sb.length(), "]").toString();
    }

}
