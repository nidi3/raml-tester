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

import guru.nidi.ramltester.core.RamlViolations;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RamlValidatorTest extends HighlevelTestBase {

    private static RamlDefinition example = RamlLoaders.fromClasspath(RamlValidatorTest.class).load("example.raml");

    @Test
    public void simple() throws Exception {
        final RamlViolations violations = example.validate();
        assertEquals(2, violations.size());
        final Iterator<String> it = violations.iterator();
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok)  mime-type('application/json')\n" +
                "Content: 42\n" +
                "Message: The content to match the given JSON schema."));
        assertThat(it.next(), startsWith("Example does not match schema for action(POST /nok) response(200) mime-type('application/json')\n" +
                "Content: 42\n" +
                "Message: The content to match the given JSON schema."));
    }

}
