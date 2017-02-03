raml-tester 
===========
[![Build Status](https://travis-ci.org/nidi3/raml-tester.svg?branch=master)](https://travis-ci.org/nidi3/raml-tester)
[![codecov](https://codecov.io/gh/nidi3/raml-tester/branch/raml-parser-2/graph/badge.svg)](https://codecov.io/gh/nidi3/raml-tester/branch/raml-parser-2)
[![License](https://img.shields.io/github/license/mashape/apistatus.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Test if a request/response matches a given raml definition.

Versioning
--

Version | Contents
---|---
0.8.x | Stable version, uses [RAML parser 0.8.x](https://github.com/raml-org/raml-java-parser/tree/v1) and supports only RAML v0.8
0.9.x | Development version, uses [RAML parser 1.x](https://github.com/raml-org/raml-java-parser) and supports RAML v0.8 and [parts of v1.0](https://github.com/nidi3/raml-tester/blob/raml-parser-2/1.0-support.md)
1.0.x | As soon as RAML v1.0 support is stable

Add it to a project
-------------------
Add these lines to the `pom.xml`:

```xml
<dependency>
    <groupId>guru.nidi.raml</groupId>
    <artifactId>raml-tester</artifactId>
    <version>0.8.8</version>
</dependency>
```

If you are stuck with java 1.6, use the compatible version by adding the following line:

```xml
    <classifier>jdk6</classifier>
```

Use in a spring MVC test
------------------------
[//]: # (spring)
```java
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class SpringTest {

    private static RamlDefinition api = RamlLoaders.fromClasspath(SpringTest.class).load("api.raml")
            .assumingBaseUri("http://nidi.guru/raml/simple/v1");
    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void greeting() throws Exception {
        Assert.assertThat(api.validate(), validates());

        mockMvc.perform(get("/greeting").accept(MediaType.parseMediaType("application/json")))
                .andExpect(api.matches().aggregating(aggregator));
    }
}
```
[//]: # (end)

The `ExpectedUsage` rule checks if all resources, query parameters, form parameters, headers and response codes
defined in the RAML are at least used once.

The `RamlMatchers.validates()` matcher validates the RAML itself.
 
`api.matches()` checks that the request/response match the RAML definition.

See also the [raml-tester-uc-spring](https://github.com/nidi3/raml-tester-uc-spring) project.

Use in a Java EE / JAX-RS environment
-------------------------------------
[//]: # (jaxrs)
```java
@RunWith(Arquillian.class)
public class JaxrsTest {

    private static RamlDefinition api = RamlLoaders.fromClasspath(JaxrsTest.class).load("api.raml")
            .assumingBaseUri("http://nidi.guru/raml/simple/v1");
    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();
    private static WebTarget target;

    @ClassRule
    public static ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(Application.class);
    }

    @ArquillianResource
    private URL base;

    @Before
    public void setup() throws MalformedURLException {
        Client client = ClientBuilder.newClient();
        target = client.target(URI.create(new URL(base, "app/path").toExternalForm()));
    }

    @Test
    public void greeting() throws Exception {
        assertThat(api.validate(), validates());

        final CheckingWebTarget webTarget = api.createWebTarget(target).aggregating(aggregator);
        webTarget.request().post(Entity.text("apple"));

        assertThat(webTarget.getLastReport(), checks());
    }
}
```
[//]: # (end)

The `RamlMatchers.checks()` matcher validates that the request and response conform to the RAML.


Use in a pure servlet environment
---------------------------------
[//]: # (servlet)
```java
public class RamlFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RamlDefinition api;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
        log.info(api.validate().toString());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final RamlReport report = api.testAgainst(request, response, chain);
        log.info("Raml report: " + report);
    }

    @Override
    public void destroy() {
    }
}
```
[//]: # (end)

Or see the [raml-tester-uc-sevlet](https://github.com/nidi3/raml-tester-uc-servlet) project.

Use together with Apache HttpComponents
---------------------------------------

[//]: # (httpComponents)
```java
public class HttpComponentsTest {
    @Test
    public void testRequest() throws IOException {
        RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
        Assert.assertThat(api.validate(), validates());

        RamlHttpClient client = api.createHttpClient();
        HttpGet get = new HttpGet("http://test.server/path");
        HttpResponse response = client.execute(get);

        Assert.assertThat(client.getLastReport(), checks());
    }
}
```
[//]: # (end)

Or see the [raml-tester-uc-servlet](https://github.com/nidi3/raml-tester-uc-servlet) project.

Use together with RestAssured
---------------------------------------
[//]: # (restAssured)
```java
public class RestAssuredTest {
    @Test
    public void testWithRestAssured() {
        RestAssured.baseURI = "http://test.server/path";
        RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
        Assert.assertThat(api.validate(), validates());

        RestAssuredClient restAssured = api.createRestAssured();
        restAssured.given().get("/base/data").andReturn();
        Assert.assertThat(restAssured.getLastReport(), hasNoViolations());
    }
}
```
[//]: # (end)
If you are using RestAssured 3.0, call `api.createRestAssured3()`.

Use as a standalone proxy
-------------------------
When used as a proxy, any service can be tested, regardless of the technology used to implement it.
See the [raml-proxy](https://github.com/nidi3/raml-tester-proxy) project.

Use with Javascript
-------------------
There is special support for javascript.

See [raml-tester-js](https://github.com/nidi3/raml-tester-js) for details and
[raml-tester-uc-js](https://github.com/nidi3/raml-tester-uc-js) for examples.



FailFast
---------------------------------------
You can configure the RamlDefinition to throw an exception in case a violation is found.

[//]: # (failFast)
```java
@Test(expected = RamlViolationException.class)
public void testInvalidResource() {
    RestAssured.baseURI = "http://test.server/path";
    RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
    Assert.assertThat(api.validate(), validates());

    RestAssuredClient restAssured = api.failFast().createRestAssured();
    restAssured.given().get("/wrong/path").andReturn();
    fail("Should throw RamlViolationException");
}
```
[//]: # (end)
