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
package guru.nidi.ramltester;

import guru.nidi.ramltester.core.RamlReport;
import org.junit.Test;

import static guru.nidi.ramltester.core.Validation.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 *
 */
public class ValidatorTest extends HighlevelTestBase {
    private static final RamlLoaders RAML_LOADERS = RamlLoaders.fromClasspath(ValidatorTest.class);
    private static final RamlDefinition
            example = RAML_LOADERS.load("example.raml"),
            uriParams = RAML_LOADERS.load("uriParameters.raml"),
            description = RAML_LOADERS.load("description.raml"),
            noDocTitle = RAML_LOADERS.load("description-no-title.raml"),
            noDocContent = RAML_LOADERS.load("description-no-content.raml");

    @Test
    public void example() {
        final RamlReport report = example.validator().withChecks(EXAMPLE).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("example of queryParameter 'q' in action(POST /ok) - Value '10' is bigger than maximum 8"),
                equalTo("default value of queryParameter 'q' in action(POST /ok) - Value '2' is smaller than minimum 4"),
                startsWith("Example does not match schema for action(POST /nok) mime-type('application/json')\n" +
                        "Content: 42\n" +
                        "Messages:\n- error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"),
                startsWith("Example does not match schema for action(POST /nok) response(200) mime-type('application/json')\n" +
                        "Content: 42\n" +
                        "Messages:\n- error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"));
    }

    @Test
    public void parameter() {
        final RamlReport report = example.validator().withChecks(PARAMETER).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("queryParameter 'a' in action(POST /ok) has illegal condition 'minimum'"),
                equalTo("queryParameter 'b' in action(POST /ok) has illegal condition 'pattern'"),
                equalTo("queryParameter 'b' in action(POST /ok) has illegal condition 'minimum'"),
                equalTo("queryParameter 'c' in action(POST /ok) has illegal condition 'pattern'"),
                equalTo("queryParameter 'd' in action(POST /ok): File type is only allowed in formParameter"),
                equalTo("No formParameter allowed in action(POST /ok) mime-type('application/json') (only allowed with 'application/x-www-form-urlencoded' or 'multipart/form-data')"));
    }

    @Test
    public void validUriParameters() {
        final RamlReport report = uriParams.validator().withChecks(URI_PARAMETER).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("The baseUri has no variable 'invalid' in Root definition"),
                equalTo("baseUriParameter with name 'version' is not allowed in Root definition"),
                equalTo("The uri has no variable 'uriInvalid' in resource(/bla/{param})"),
                equalTo("uriParameter with name 'version' is not allowed in resource(/bla/{param})"),
                equalTo("The baseUri has no variable 'subInvalid' in resource(/bla/{param})"),
                equalTo("The uri has no variable 'subinvalid' in resource(/bla/{param}/subA/{p})"),
                equalTo("The baseUri has no variable 'actioninvalid' in action(GET /bla/{param})"));
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
                equalTo("baseUriParameter name 'subInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'"),
                equalTo("uriParameter name 'uriInvalid' in resource(/bla/{param}) does not match pattern '[a-z]+'"),
                equalTo("queryParameter name 'Nok' in action(GET /bla/{param}) does not match pattern '[a-z]+'"),
                equalTo("formParameter name 'Form' in action(GET /bla/{param}) mime-type('application/x-www-form-urlencoded') does not match pattern '[a-z]+'"));
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
                equalTo("Root definition has no documentation"),
                equalTo("baseUriParameter 'path' in Root definition has no description"),
                equalTo("resource(/bla/{param}) has no description"),
                equalTo("baseUriParameter 'path' in resource(/bla/{param}) has no description"),
                equalTo("uriParameter 'param' in resource(/bla/{param}) has no description"),
                equalTo("action(GET /bla/{param}) has no description"),
                equalTo("baseUriParameter 'actioninvalid' in action(GET /bla/{param}) has no description"),
                equalTo("queryParameter 'ok' in action(GET /bla/{param}) has no description"),
                equalTo("header 'ok' in action(GET /bla/{param}) has no description"),
                equalTo("formParameter 'Form' in action(GET /bla/{param}) mime-type('application/x-www-form-urlencoded') has no description"),
                equalTo("action(GET /bla/{param}) response(200) has no description"),
                equalTo("header 'ok' in action(GET /bla/{param}) response(200) has no description"));
    }

    @Test
    public void missingDocTitle() {
        final RamlReport report = noDocTitle.validator().withChecks(DESCRIPTION).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("Root definition has documentation with missing title"));
    }

    @Test
    public void missingDocContent() {
        final RamlReport report = noDocContent.validator().withChecks(DESCRIPTION).validate();
        assertViolationsThat(report.getValidationViolations(),
                equalTo("Root definition has documentation with missing content"));
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
