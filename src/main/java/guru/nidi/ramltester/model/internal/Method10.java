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

import org.raml.v2.api.model.v10.methods.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

class Method10 implements RamlMethod {
    private final Method method;

    Method10(Method method) {
        this.method = method;
    }

    static List<RamlMethod> of(List<Method> methods) {
        final List<RamlMethod> res = new ArrayList<>();
        for (final Method m : methods) {
            res.add(new Method10(m));
        }
        return res;
    }

    @Override
    public String method() {
        return method.method().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public RamlResource resource() {
        return new Resource10(method.resource());
    }

    @Override
    public List<String> protocols() {
        return method.protocols();
    }

    @Override
    public List<RamlType> queryParameters() {
        return Type10.of(method.queryString() == null ? method.queryParameters() : Collections.singletonList(method.queryString()));
    }

    @Override
    public List<RamlType> headers() {
        return Type10.of(method.headers());
    }

    @Override
    public String description() {
        return method.description() == null ? null : method.description().value();
    }

    @Override
    public List<RamlType> baseUriParameters() {
        return emptyList();
    }

    @Override
    public List<RamlApiResponse> responses() {
        return Response10.of(method.responses());
    }

    @Override
    public List<RamlSecSchemeRef> securedBy() {
        return SecSchemeRef10.of(method.securedBy());
    }

    @Override
    public List<RamlBody> body() {
        return Body10.of(method.body());
    }

}
