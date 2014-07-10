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

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 */
public class FormTest extends HighlevelTestBase {
    private RamlDefinition simple = RamlLoaders.fromClasspath(getClass()).load("form.raml");

    @Test
    public void formTest() throws Exception {
        assertNoViolations(
                simple,
                post("/form").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("param","a+b"),
                jsonResponse(200, "", null));
    }

    @Test
    public void undefinedParam() throws Exception {
        assertOneRequestViolationThat(
                simple,
                post("/form").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("hula","a+b"),
                jsonResponse(200, "", null),
                equalTo("Form parameter 'hula' on action(POST /form) is not defined"));
    }

    @Test
    public void noContentType() throws Exception {
        assertOneRequestViolationThat(
                simple,
                post("/form").content("param=a+b"),
                jsonResponse(200, "", null),
                equalTo("No Content-Type header given"));
    }

    @Test
    public void formWithoutFormParameters() throws Exception {
        assertOneRequestViolationThat(
                simple,
                post("/form/parameterless").contentType(MediaType.APPLICATION_FORM_URLENCODED),
                jsonResponse(200, "", null),
                equalTo("No formParameters given on action(POST /form/parameterless) mime-type('application/x-www-form-urlencoded')"));
    }

    @Test
    public void formWithSchema() throws Exception {
        final RamlDefinition simple = RamlLoaders
                .fromClasspath(getClass())
                .addSchemaValidator(new FormEncodedSchemaValidator())
                .load("form.raml");

        assertOneRequestViolationThat(
                simple,
                post("/form/schema").contentType(MediaType.APPLICATION_FORM_URLENCODED),
                jsonResponse(200, "", null),
                equalTo("No schema allowed on action(POST /form/schema) mime-type('application/x-www-form-urlencoded')"))
        ;
    }

    @Test
    public void multipartForm() throws Exception {
        assertNoViolations(
                simple,
                fileUpload("/form/multi").file("file", new byte[]{1, 2, 3})
                        .contentType(MediaType.MULTIPART_FORM_DATA).param("param", "a +b"),
                jsonResponse(200, "", null)
        );
    }

}
