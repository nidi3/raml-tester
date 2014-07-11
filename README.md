raml-tester [![Build Status](https://travis-ci.org/nidi3/raml-tester.svg?branch=master)](https://travis-ci.org/nidi3/raml-tester)
===========

Test if a request/response matches a given raml definition.

Use in a spring MVC test
------------------------
```
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class SimpleTest {

    private static RamlDefinition api = RamlLoaders.fromClasspath(SimpleTest.class).load("api.raml")
        .assumingBaseUri("http://nidi.guru/raml/simple/v1");
    private static SimpleReportAggregator aggregator = new SimpleReportAggregator();

    @ClassRule
    public static ExpectedCoverage expectedCoverage = new ExpectedCoverage(aggregator);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void greeting() throws Exception {
        mockMvc.perform(get("/greeting").accept(MediaType.parseMediaType("application/json")))
                .andExpect(api.matches().aggregating(aggregator));
    }

}
```
The ExpectedCoverage rule additionally checks if all resources, query parameters, form parameters, headers and response codes
defined in the RAML are at least used once.
See also the demo project https://github.com/nidi3/raml-tester-uc-spring


Use in a pure servlet environment
---------------------------------
```
public class RamlFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RamlDefinition api;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                         throws IOException, ServletException {
        final RamlReport report = api.testAgainst(request, response, chain);
        log.info("Raml report: " + report);
    }

    @Override
    public void destroy() {}
}

```
Or see the demo project https://github.com/nidi3/raml-tester-uc-servlet

Use together with Apache HttpComponents
---------------------------------------
```
@Test
public void testRequest(){
    RamlDefinition api = RamlLoaders.fromClasspath(getClass()).load("api.yaml");
    RamlHttpClient client = api.createHttpClient();
    HttpGet get = new HttpGet("http://test.server/path");
    HttpResponse response = client.execute(get);
    Assert.assertTrue(client.getLastResport().isEmpty());
}

```
Or see the demo project https://github.com/nidi3/raml-tester-uc-servlet

Use as a standalone proxy
-------------------------
When used as a proxy, any service can be tested, regardless of the technology used to implement it.
See raml-proxy project: https://github.com/nidi3/raml-tester-proxy