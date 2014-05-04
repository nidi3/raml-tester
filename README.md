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
    private Raml api;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        api = RamlLoaders.fromClasspath(getClass(), "api.yaml");
    }

    @Test
    public void greeting() throws Exception {
        this.mockMvc.perform(get("/greeting").accept(MediaType.parseMediaType("application/json")))
                .andExpect(RamlMatchers.matchesRaml(api));
    }

}
```
Or see the demo project https://github.com/nidi3/raml-tester-uc-spring


Use in a pure servlet environment
---------------------------------
```
public class RamlFilter implements Filter {
    private Raml api;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        api = RamlLoaders.fromClasspath(getClass(), "api.yaml");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final RamlViolations violations = RamlTesters.executeFilterChain(api, request, response, chain);
        System.out.log("Violations: " + violations);
    }

    @Override
    public void destroy() {

    }
}

```
Or see the demo project https://github.com/nidi3/raml-tester-uc-servlet