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
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolationException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static guru.nidi.ramltester.core.Validation.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class ValidatorTest extends HighlevelTestBase {
    private static final RamlLoaders RAML_LOADERS = RamlLoaders.fromClasspath(ValidatorTest.class);
    private static final RamlDefinition
            example = RAML_LOADERS.load("example.raml"),
            uriParams = RAML_LOADERS.load("uriParameters.raml"),
            description = RAML_LOADERS.load("description.raml");

    @Test
    public void wrongTypeConstraints() {
        try {
            RAML_LOADERS.load("wrong-types.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            final List<String> violations = e.getReport().getValidationViolations().asList();
            assertThat(violations.get(0), containsString("Duplicated key '/nonEmpty'"));
            assertThat(violations.get(1), containsString("Unexpected key 'minimum'"));
            assertThat(violations.get(2), containsString("Unexpected key 'pattern'"));
            assertThat(violations.get(3), containsString("Unexpected key 'minimum'"));
            assertThat(violations.get(4), containsString("Unexpected key 'pattern'"));
        }
    }

    @Test
    public void missingDocTitle() {
        //TODO remove check, done by parser now
        try {
            RAML_LOADERS.load("description-no-title.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            final List<String> violations = e.getReport().getValidationViolations().asList();
            assertThat(violations.get(0), containsString("Missing required field \"title\""));
        }
    }

    @Test
    public void missingDocContent() {
        //TODO remove check, done by parser now
        try {
            RAML_LOADERS.load("description-no-content.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            final List<String> violations = e.getReport().getValidationViolations().asList();
            assertThat(violations.get(0), containsString("Missing required field \"content\""));
        }
    }

    @Test
    public void example() {
        final RamlReport report = example.validator().withChecks(EXAMPLE).validate();
        assertEquals(5, report.getValidationViolations().size());
        final Iterator<String> it = report.getValidationViolations().iterator();
        assertEquals("example of queryParameter 'q' in action(POST /ok) - Value '10': Expected number between 4 and 8", it.next());
        assertEquals("default value of queryParameter 'q' in action(POST /ok) - Value '2': Expected number between 4 and 8", it.next());
        assertEquals("example of queryParameter 'u' in action(POST /ok) - Value '{\"name\":\"n\"}\n': " +
                "Error validating JSON. Error: - Missing required field \"firstname\"\n" +
                "- Missing required field \"lastname\"\n" +
                "- Missing required field \"age\"",it.next());
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok) mime-type('application/json')\n" +
                "Content: 42\n\n" +
                "Message: error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"));
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok) response(200) mime-type('application/json')\n" +
                "Content: 42\n\n" +
                "Message: error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"));
    }

    @Test
    public void validUriParameters() {
        final RamlReport report = uriParams.validator().withChecks(URI_PARAMETER).validate();
        assertEquals(Arrays.asList(
                "The baseUri has no variable 'invalid' in Root definition",
                "baseUriParameter with name 'version' is not allowed in Root definition",
                "The uri has no variable 'uriInvalid' in resource(/bla/{param})",
                "uriParameter with name 'version' is not allowed in resource(/bla/{param})",
                "The uri has no variable 'subinvalid' in resource(/bla/{param}/subA/{p})"),
                report.getValidationViolations().asList());

    }

    @Test
    public void resourcePattern() {
        final RamlReport report = uriParams.validator().withChecks().withResourcePattern("[a-z]+").validate();
        assertEquals(Arrays.asList(
                "Name of resource(/bla/{param}/subA/{p}) does not match pattern '[a-z]+'"),
                report.getValidationViolations().asList());

    }

    @Test
    public void parameterPattern() {
        final RamlReport report = uriParams.validator().withChecks().withParameterPattern("[a-z]+").validate();
        assertEquals(Arrays.asList(
                "uriParameter name 'uriInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'",
                "queryParameter name 'Nok' in action(GET /bla/{param}) does not match pattern '[a-z]+'"),
                report.getValidationViolations().asList());
    }

    @Test
    public void headerPattern() {
        final RamlReport report = uriParams.validator().withChecks().withHeaderPattern("[a-z]+").validate();
        assertEquals(Arrays.asList(
                "header name 'Hok' in action(GET /bla/{param}) does not match pattern '[a-z]+'",
                "header name 'Rok' in action(GET /bla/{param}) response(200) does not match pattern '[a-z]+'"),
                report.getValidationViolations().asList());
    }

    @Test
    public void description() {
        final RamlReport report = description.validator().withChecks(DESCRIPTION).validate();
        assertEquals(Arrays.asList(
                "Root definition has no documentation",
                "baseUriParameter 'path' in Root definition has no description",
                "resource(/bla/{param}) has no description",
                "uriParameter 'param' in resource(/bla/{param}) has no description",
                "action(GET /bla/{param}) has no description",
                "queryParameter 'ok' in action(GET /bla/{param}) has no description",
                "header 'ok' in action(GET /bla/{param}) has no description",
                "action(GET /bla/{param}) response(200) has no description",
                "header 'ok' in action(GET /bla/{param}) response(200) has no description"),
                report.getValidationViolations().asList());
    }


    @Test
    public void empty() {
        final RamlReport report = example.validator().withChecks(EMPTY).validate();
        assertEquals(Arrays.asList(
                "resource(/empty) is empty",
                "action(GET /nonEmpty/sub) is empty"),
                report.getValidationViolations().asList());
    }
}
