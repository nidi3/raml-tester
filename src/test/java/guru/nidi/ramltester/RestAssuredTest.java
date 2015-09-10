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

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import guru.nidi.ramltester.restassured.RamlValidationException;
import guru.nidi.ramltester.restassured.RamlValidationFilter;
import guru.nidi.ramltester.util.ServerTest;

/**
 *
 */
public class RestAssuredTest extends ServerTest {

	private RamlDefinition api;

	@Before
	public void before() {
		this.api = RamlLoaders.fromClasspath(RestAssuredTest.class).load("restAssured.raml")
				.assumingBaseUri("http://nidi.guru/raml/v1");
		RestAssured.baseURI = baseUrl();

	}

	@Override
	protected int port() {
		return 8082;
	}

	@Test
	public void testServletOk() throws IOException {
		given().filter(new RamlValidationFilter(api)).get("/base/data").andReturn();
	}

	@Test
	public void testServletNok() throws IOException {

		try {
			given().filter(new RamlValidationFilter(api)).get("/base/data?param=bu").andReturn();
			fail();
		} catch (RamlValidationException e) {
			assertEquals(
					"RamlReport{requestViolations=[Query parameter 'param' on action(GET /base/data) is not defined], responseViolations=[Body does not match schema for action(GET /base/data) response(200) mime-type('application/json')\n" + 
					"Content: illegal json\n" + 
					"Message: Schema invalid: com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'illegal': was expecting ('true', 'false' or 'null')\n" + 
					" at [Source: Body; line: 1, column: 8]], validationViolations=[]}",
					e.getMessage());
		}
	}

	@Test
	public void emptyResponse() throws IOException {
		Response response = given().filter(new RamlValidationFilter(api)).get("/base/data?empty=yes").andReturn();
		assertEquals(HttpStatus.SC_NO_CONTENT, response.statusCode());
		assertTrue(StringUtils.isBlank(response.getBody().asString()));
	}

	private static class TestServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			if (req.getParameter("empty") != null) {
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				res.setContentType("application/json");
				final PrintWriter out = res.getWriter();
				out.write(req.getParameter("param") == null ? "\"json string\"" : "illegal json");
				out.flush();
			}
		}
	}

	@Override
	protected void init(Context ctx) {
		Tomcat.addServlet(ctx, "app", new TestServlet());
		ctx.addServletMapping("/*", "app");
	}
}