package guru.nidi.ramltester.restassured;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.core.RamlReport;


public class RamlValidationFilter implements Filter {

	private final RamlDefinition api;

	public RamlValidationFilter(RamlDefinition api) {
		this.api = api;
	}

	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext filterContext) {
		Response response = filterContext.next(requestSpec, responseSpec);
		RamlReport report = api.testAgainst(new RestAssuredRamlRequest(requestSpec, filterContext),
				new RestAssuredRamlResponse(response));
		
		if (!report.isEmpty()) {
			throw new RamlValidationException(report.toString());
		}
		return response;

	}
}
