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
package guru.nidi.ramltester.spring;

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FileValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
@Controller
public class SpringMockRamlMessageTest {
    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(this).build();
    }

    @RequestMapping(value = "path", produces = "text/dummy")
    @ResponseBody
    public ResponseEntity<String> test() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("head", "resValue");
        return new ResponseEntity<>("responsö", headers, HttpStatus.ACCEPTED);
    }

    @Test
    public void simple() throws Exception {
        final MvcResult result = mockMvc.perform(get("http://test.com/path?param=val")
                .characterEncoding("utf-8")
                .content("contentä")
                .contentType(MediaType.TEXT_PLAIN)
                .param("param2", "val2")
                .header("head", "hval")).andReturn();

        final SpringMockRamlRequest ramlRequest = new SpringMockRamlRequest(result.getRequest());

        assertEquals("contentä", new String(ramlRequest.getContent(), "utf-8"));
        assertEquals("text/plain", ramlRequest.getContentType());

        final Values formValues = new Values().addValue("param", "val").addValue("param2", "val2");
        assertEquals(formValues, ramlRequest.getFormValues());

        final Values headerValues = new Values().addValue("head", "hval").addValue("Content-Type", "text/plain;charset=utf-8");
        assertEquals(headerValues, ramlRequest.getHeaderValues());

        assertEquals("GET", ramlRequest.getMethod());
        assertEquals(new Values().addValue("param", "val"), ramlRequest.getQueryValues());
        assertEquals("http://test.com/path", ramlRequest.getRequestUrl(null));
        assertEquals("http://x.y/path", ramlRequest.getRequestUrl("http://x.y"));

        final SpringMockRamlResponse ramlResponse = new SpringMockRamlResponse(result.getResponse());

        assertEquals("responsö", new String(ramlResponse.getContent(), "iso-8859-1"));
        assertEquals("text/dummy", ramlResponse.getContentType());

        final Values resHeaderValues = new Values().addValue("head", "resValue").addValue("Content-Length", "" + ("responsö".length())).addValue("Content-Type", "text/dummy");
        assertEquals(resHeaderValues, ramlResponse.getHeaderValues());

        assertEquals(202, ramlResponse.getStatus());
    }

    @Test
    public void multipart() throws Exception {
        final MvcResult result = mockMvc.perform(fileUpload("http://test.com/path?param=val")
                .file("file", new byte[]{})
                .characterEncoding("utf-8")
                .content("contentä")
                .param("param2", "val2")
                .header("head", "hval")).andReturn();

        final SpringMockRamlRequest ramlRequest = new SpringMockRamlRequest(result.getRequest());

        assertEquals("contentä", new String(ramlRequest.getContent(), "utf-8"));
        assertEquals("multipart/form-data", ramlRequest.getContentType());

        final Values formValues = new Values().addValue("param", "val").addValue("param2", "val2").addValue("file", new FileValue());
        assertEquals(formValues, ramlRequest.getFormValues());

        final Values headerValues = new Values().addValue("head", "hval").addValue("Content-Type", "multipart/form-data;charset=utf-8");
        assertEquals(headerValues, ramlRequest.getHeaderValues());

        assertEquals("POST", ramlRequest.getMethod());
        assertEquals(new Values().addValue("param", "val"), ramlRequest.getQueryValues());
        assertEquals("http://test.com/path", ramlRequest.getRequestUrl(null));
        assertEquals("http://x.y/path", ramlRequest.getRequestUrl("http://x.y"));
    }
}
