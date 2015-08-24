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

import guru.nidi.ramltester.core.RamlReport;
import org.junit.Test;

import java.util.Iterator;

import static guru.nidi.ramltester.core.Validation.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RamlValidatorTest extends HighlevelTestBase {
    private static RamlDefinition example = RamlLoaders.fromClasspath(RamlValidatorTest.class).load("example.raml");
    private static RamlDefinition uriParams = RamlLoaders.fromClasspath(RamlValidatorTest.class).load("uriParameters.raml");
    private static RamlDefinition description = RamlLoaders.fromClasspath(RamlValidatorTest.class).load("description.raml");

    @Test
    public void example() {
        final RamlReport report = example.validator().withChecks(EXAMPLE).validate();
        assertEquals(4, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("example of queryParameter 'q' in action(POST /ok) - Value '10' is bigger than maximum 8", it.next());
        assertEquals("default value of queryParameter 'q' in action(POST /ok) - Value '2' is smaller than minimum 4", it.next());
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok) mime-type('application/json')\n" +
                "Content: 42\n" +
                "Message: The content to match the given JSON schema."));
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok) response(200) mime-type('application/json')\n" +
                "Content: 42\n" +
                "Message: The content to match the given JSON schema."));
    }

    @Test
    public void parameter() {
        final RamlReport report = example.validator().withChecks(PARAMETER).validate();
        assertEquals(6, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("queryParameter 'a' in action(POST /ok) has illegal condition 'minimum'", it.next());
        assertEquals("queryParameter 'b' in action(POST /ok) has illegal condition 'pattern'", it.next());
        assertEquals("queryParameter 'b' in action(POST /ok) has illegal condition 'minimum'", it.next());
        assertEquals("queryParameter 'c' in action(POST /ok) has illegal condition 'pattern'", it.next());
        assertEquals("queryParameter 'd' in action(POST /ok): File type is only allowed in formParameter", it.next());
        assertEquals("No formParameter allowed in action(POST /ok) mime-type('application/json') (only allowed with 'application/x-www-form-urlencoded' or 'multipart/form-data')", it.next());
    }

    @Test
    public void validUriParameters() {
        final RamlReport report = uriParams.validator().withChecks(URI_PARAMETER).validate();
        assertEquals(7, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("The baseUri has no variable 'invalid' in Root definition", it.next());
        assertEquals("baseUriParameter with name 'version' is not allowed in Root definition", it.next());
        assertEquals("The uri has no variable 'uriInvalid' in resource(/bla/{param})", it.next());
        assertEquals("uriParameter with name 'version' is not allowed in resource(/bla/{param})", it.next());
        assertEquals("The baseUri has no variable 'subInvalid' in resource(/bla/{param})", it.next());
        assertEquals("The uri has no variable 'subinvalid' in resource(/bla/{param}/subA/{p})", it.next());
        assertEquals("The baseUri has no variable 'actioninvalid' in action(GET /bla/{param})", it.next());
    }

    @Test
    public void resourcePattern() {
        final RamlReport report = uriParams.validator().withChecks().withResourcePattern("[a-z]+").validate();
        assertEquals(1, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("Name of resource(/bla/{param}/subA/{p}) does not match pattern '[a-z]+'", it.next());

    }

    @Test
    public void parameterPattern() {
        final RamlReport report = uriParams.validator().withChecks().withParameterPattern("[a-z]+").validate();
        assertEquals(4, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("baseUriParameter name 'subInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'", it.next());
        assertEquals("uriParameter name 'uriInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'", it.next());
        assertEquals("queryParameter name 'Nok' in action(GET /bla/{param}) does not match pattern '[a-z]+'", it.next());
        assertEquals("formParameter name 'Form' in action(GET /bla/{param}) mime-type('application/x-www-form-urlencoded') does not match pattern '[a-z]+'", it.next());
    }

    @Test
    public void headerPattern() {
        final RamlReport report = uriParams.validator().withChecks().withHeaderPattern("[a-z]+").validate();
        assertEquals(2, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("header name 'Hok' in action(GET /bla/{param}) does not match pattern '[a-z]+'", it.next());
        assertEquals("header name 'Rok' in action(GET /bla/{param}) response(200) does not match pattern '[a-z]+'", it.next());
    }

    @Test
    public void description() {
        final RamlReport report = description.validator().withChecks(DESCRIPTION).validate();
        assertEquals(12, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("Root definition has no description", it.next());
        assertEquals("baseUriParameter 'path' in Root definition has no description", it.next());
        assertEquals("resource(/bla/{param}) has no description", it.next());
        assertEquals("baseUriParameter 'path' in resource(/bla/{param}) has no description", it.next());
        assertEquals("uriParameter 'param' in resource(/bla/{param}) has no description", it.next());
        assertEquals("action(GET /bla/{param}) has no description", it.next());
        assertEquals("baseUriParameter 'actioninvalid' in action(GET /bla/{param}) has no description", it.next());
        assertEquals("queryParameter 'ok' in action(GET /bla/{param}) has no description", it.next());
        assertEquals("header 'ok' in action(GET /bla/{param}) has no description", it.next());
        assertEquals("formParameter 'Form' in action(GET /bla/{param}) mime-type('application/x-www-form-urlencoded') has no description", it.next());
        assertEquals("action(GET /bla/{param}) response(200) has no description", it.next());
        assertEquals("header 'ok' in action(GET /bla/{param}) response(200) has no description", it.next());
    }

    @Test
    public void empty() {
        final RamlReport report = example.validator().withChecks(EMPTY).validate();
        assertEquals(2, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("resource(/empty) is empty", it.next());
        assertEquals("action(GET /nonEmpty/sub) is empty", it.next());
    }
}
