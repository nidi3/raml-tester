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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import guru.nidi.ramltester.util.ServerTest;

/**
 *
 */
public class RestAssuredTest extends ServerTest {

	private RestAssuredClient restAssured;
	private RamlDefinition api;

	@Before
	public void before() {
		RestAssured.baseURI = baseUrl();
		api = RamlLoaders.fromClasspath(RestAssuredTest.class).load("restAssured.raml")
				.assumingBaseUri("http://nidi.guru/raml/v1");
		this.restAssured = api.createRestAssured();

	}

	@Override
	protected int port() {
		return 8082;
	}

	@Test
	public void testServletOk() throws IOException {
		restAssured.given().get("/base/data").andReturn();
		Assert.assertTrue(restAssured.getLastReport().isEmpty());
	}

	@Test
	public void testServletNok() throws IOException {
		restAssured.given().get("/base/data?param=bu").andReturn();

		final RamlViolations requestViolations = restAssured.getLastReport().getRequestViolations();
		assertEquals(1, requestViolations.size());
		assertThat(requestViolations.iterator().next(),
				equalTo("Query parameter 'param' on action(GET /base/data) is not defined"));

		final RamlViolations responseViolations = restAssured.getLastReport().getResponseViolations();
		assertEquals(1, responseViolations.size());
		assertThat(responseViolations.iterator().next(),
				equalTo("Body does not match schema for action(GET /base/data) response(200) mime-type('application/json')\n"
						+ "Content: illegal json\n"
						+ "Message: Schema invalid: com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'illegal': was expecting ('true', 'false' or 'null')\n"
						+ " at [Source: Body; line: 1, column: 8]"));

	}

	@Test
	public void emptyResponse() throws IOException {
		Response response = restAssured.given().get("/base/data?empty=yes").andReturn();
		assertEquals(HttpStatus.SC_NO_CONTENT, response.statusCode());
		assertTrue(StringUtils.isBlank(response.getBody().asString()));
	}

	@Override
	protected void init(Context ctx) {
		Tomcat.addServlet(ctx, "app", new TestServlet());
		ctx.addServletMapping("/*", "app");
	}
}