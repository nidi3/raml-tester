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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolationException;
import org.junit.Test;

import static guru.nidi.ramltester.core.Validation.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.fail;

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
            assertViolationsThat(e.getReport().getValidationViolations(),
                    containsString("Unexpected key 'string'"),
                    containsString("Duplicated key '/nonEmpty'"),
                    containsString("Unexpected key 'minimum'"),
                    containsString("Unexpected key 'pattern'"),
                    containsString("Unexpected key 'minimum'"),
                    containsString("Unexpected key 'pattern'"));
        }
    }

    @Test
    public void typesAndSchemas() {
        try {
            RAML_LOADERS.load("types-and-schemas.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            assertViolationsThat(e.getReport().getValidationViolations(),
                    containsString("Exception during RAML check: \"types\" and \"schemas\" are mutually exclusive."));
        }
    }

    @Test
    public void missingDocTitle() {
        //TODO remove check, done by parser now
        try {
            RAML_LOADERS.load("description-no-title.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            assertViolationsThat(e.getReport().getValidationViolations(),
                    containsString("Missing required field \"title\""));
        }
    }

    @Test
    public void missingDocContent() {
        //TODO remove check, done by parser now
        try {
            RAML_LOADERS.load("description-no-content.raml");
            fail("Invalid RAML");
        } catch (RamlViolationException e) {
            assertViolationsThat(e.getReport().getValidationViolations(),
                    containsString("Missing required field \"content\""));
        }
    }

    @Test
    public void example() {
        final RamlReport report = example.validator().withChecks(EXAMPLE).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("example of annotationType 'simple' in root definition - Value 'bla': Invalid type String, expected Integer"),
                equalTo("example of queryParameter 'q' in action(POST /ok) - Value '10': Expected number between 4 and 8"),
                equalTo("default value of queryParameter 'q' in action(POST /ok) - Value '2': Expected number between 4 and 8"),
                equalTo("example of queryParameter 'u' in action(POST /ok) - Value '{\"name\":\"n\"}\n': " +
                        "Error validating JSON. Error: - Missing required field \"firstname\"\n" +
                        "- Missing required field \"lastname\"\n" +
                        "- Missing required field \"age\""),
                startsWith("Example does not match schema for action(POST /nok) mime-type('application/json')\n" +
                        "Content: 42\n\n" +
                        "Messages:\n- error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"),
                startsWith("Example does not match schema for action(POST /nok) response(200) mime-type('application/json')\n" +
                        "Content: 42\n\n" +
                        "Messages:\n- error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"));
    }

    @Test
    public void validUriParameters() {
        final RamlReport report = uriParams.validator().withChecks(URI_PARAMETER).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("The baseUri has no variable 'invalid' in root definition"),
                equalTo("baseUriParameter with name 'version' is not allowed in root definition"),
                equalTo("The uri has no variable 'uriInvalid' in resource(/bla/{param})"),
                equalTo("uriParameter with name 'version' is not allowed in resource(/bla/{param})"),
                equalTo("The uri has no variable 'subinvalid' in resource(/bla/{param}/subA/{p})"));

    }

    @Test
    public void resourcePattern() {
        final RamlReport report = uriParams.validator().withChecks().withResourcePattern("[a-z]+").validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("Name of resource(/bla/{param}/subA/{p}) does not match pattern '[a-z]+'"));
    }

    @Test
    public void parameterPattern() {
        final RamlReport report = uriParams.validator().withChecks().withParameterPattern("[a-z]+").validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("uriParameter name 'uriInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'"),
                equalTo("queryParameter name 'Nok' in action(GET /bla/{param}) does not match pattern '[a-z]+'"));
    }

    @Test
    public void headerPattern() {
        final RamlReport report = uriParams.validator().withChecks().withHeaderPattern("[a-z]+").validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("header name 'Hok' in action(GET /bla/{param}) does not match pattern '[a-z]+'"),
                equalTo("header name 'Rok' in action(GET /bla/{param}) response(200) does not match pattern '[a-z]+'"));
    }

    @Test
    public void description() {
        final RamlReport report = description.validator().withChecks(DESCRIPTION).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("root definition has no documentation"),
                equalTo("root definition has no description"),
                equalTo("baseUriParameter 'path' in root definition has no description"),
                equalTo("annotationType 'simple' in root definition has no description"),
                equalTo("resource(/bla/{param}) has no description"),
                equalTo("uriParameter 'param' in resource(/bla/{param}) has no description"),
                equalTo("action(GET /bla/{param}) has no description"),
                equalTo("queryParameter 'ok' in action(GET /bla/{param}) has no description"),
                equalTo("header 'ok' in action(GET /bla/{param}) has no description"),
                equalTo("action(GET /bla/{param}) response(200) has no description"),
                equalTo("header 'ok' in action(GET /bla/{param}) response(200) has no description"));
    }


    @Test
    public void empty() {
        final RamlReport report = example.validator().withChecks(EMPTY).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("resource(/empty) is empty"),
                equalTo("action(GET /nonEmpty/sub) is empty"),
                equalTo("action(GET /nonEmpty) is empty"));
    }
}
