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
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import org.junit.Test;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;

import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;


public class IncludeTest extends HighlevelTestBase {
    private final RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("include.raml");

    @Test
    public void load() throws Exception {
        final Api raml = api.getModel().getApiV10();
        final Method get = raml.resources().get(0).methods().get(0);
        assertNull(paramByName(get.queryParameters(), "string.json"));
        assertNotEquals("string.json", responseByCode(get.responses(), "200").body().get(0).example());
        assertNotEquals("string.json", responseByCode(get.responses(), "201").body().get(0).type());
        assertNotEquals("string.json", responseByCode(get.responses(), "201").body().get(0).example());
    }

    private TypeDeclaration paramByName(List<TypeDeclaration> parameters, String name) {
        for (final TypeDeclaration parameter : parameters) {
            if (parameter.name().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    private Response responseByCode(List<Response> responses, String code) {
        for (final Response response : responses) {
            if (response.code().value().equals(code)) {
                return response;
            }
        }
        return null;
    }

}
