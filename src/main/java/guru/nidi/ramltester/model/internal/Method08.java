/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v08.methods.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Method08 implements RamlMethod {
    private final Method method;

    Method08(Method method) {
        this.method = method;
    }

    static List<RamlMethod> of(List<Method> methods) {
        final List<RamlMethod> res = new ArrayList<>();
        for (final Method m : methods) {
            res.add(new Method08(m));
        }
        return res;
    }

    @Override
    public String method() {
        return method.method().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public RamlResource resource() {
        return new Resource08(method.resource());
    }

    @Override
    public List<String> protocols() {
        return method.protocols();
    }

    @Override
    public List<RamlType> queryParameters() {
        return Type08.of(method.queryParameters());
    }

    @Override
    public List<RamlType> headers() {
        return Type08.of(method.headers());
    }

    @Override
    public String description() {
        return method.description() == null ? null : method.description().value();
    }

    @Override
    public List<RamlType> baseUriParameters() {
        return Type08.of(method.baseUriParameters());
    }

    @Override
    public List<RamlApiResponse> responses() {
        return Response08.of(method.responses());
    }

    @Override
    public List<RamlSecSchemeRef> securedBy() {
        return SecSchemeRef08.of(method.securedBy());
    }

    @Override
    public List<RamlBody> body() {
        return Body08.of(method.body());
    }
}
