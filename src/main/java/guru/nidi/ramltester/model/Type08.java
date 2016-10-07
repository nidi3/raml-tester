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
import org.raml.v2.api.model.v08.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 *
 */
public class Type08 implements UnifiedType {
    private Parameter parameter;

    public Type08(Parameter parameter) {
        this.parameter = parameter;
    }

    static List<UnifiedType> of(List<Parameter> parameters) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final Parameter p : parameters) {
            res.add(new Type08(p));
        }
        return res;
    }

    @Override
    public String name() {
        return parameter.name();
    }

    @Override
    public String description() {
        return parameter.description() == null ? null : parameter.description().value();
    }

    @Override
    public List<String> examples() {
        return singletonList(parameter.example());
    }

    @Override
    public String defaultValue() {
        return parameter.defaultValue();
    }

    @Override
    public boolean required() {
        return parameter.required();
    }

    @Override
    public boolean repeat() {
        return parameter.repeat() != null && parameter.repeat();
    }

    @Override
    public void validate(Object payload, RamlViolations violations, Message message) {
        new ParameterChecker08(violations).checkParameter(parameter, payload, message);
    }
}