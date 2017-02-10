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

import org.raml.v2.api.model.v08.bodies.BodyLike;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

class Body08 implements RamlBody {
    private final BodyLike body;

    Body08(BodyLike body) {
        this.body = body;
    }

    static List<RamlBody> of(List<BodyLike> bodies) {
        final List<RamlBody> res = new ArrayList<>();
        for (final BodyLike b : bodies) {
            res.add(new Body08(b));
        }
        return res;
    }

    @Override
    public BodyLike getDelegate() {
        return body;
    }

    @Override
    public String name() {
        return body.name();
    }

    @Override
    public List<RamlType> formParameters() {
        return Type08.of(body.formParameters());
    }

    @Override
    public String typeDefinition() {
        return body.schemaContent();
    }

    @Override
    public String type() {
        return body.schema() == null ? null : body.schema().value();
    }

    @Override
    public List<String> examples() {
        return singletonList(body.example().value());
    }
}
