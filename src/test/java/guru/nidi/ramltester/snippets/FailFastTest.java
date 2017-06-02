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
package guru.nidi.ramltester.snippets;

import com.jayway.restassured.RestAssured;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.core.RamlViolationException;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static guru.nidi.ramltester.junit.RamlMatchers.validates;
import static org.junit.Assert.fail;

@Ignore
public class FailFastTest {

    //## failFast
    @Test(expected = RamlViolationException.class)
    public void testInvalidResource() {
        RestAssured.baseURI = "http://test.server/path";
        RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
        Assert.assertThat(api.validate(), validates());

        RestAssuredClient restAssured = api.failFast().createRestAssured();
        restAssured.given().get("/wrong/path").andReturn();
        fail("Should throw RamlViolationException");
    }
//##
}
