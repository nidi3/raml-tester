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

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private RamlMatcher apiMatches;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RamlDefinition api = RamlDefinition.load("api.yaml").fromClasspath(getClass());
        apiMatches = api.matches().assumingServletUri("http://nidi.guru/raml/simple/v1");
    }

    @Test
    public void greeting() throws Exception {
        mockMvc.perform(get("/greeting").accept(MediaType.parseMediaType("application/json")))
                .andExpect(apiMatches);
    }

}
```
Or see the demo project https://github.com/nidi3/raml-tester-uc-spring


Use in a pure servlet environment
---------------------------------
```
public class RamlFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RamlDefinition api;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        api = RamlDefinition.load("api.yaml").fromClasspath(getClass());
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