package guru.nidi.ramltester.restassured;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.specification.FilterableRequestSpecification;

import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;

class RestAssuredRamlRequest extends RestAssuredRaml implements RamlRequest {

	private final FilterableRequestSpecification requestSpec;
	private final FilterContext filterContext;

	RestAssuredRamlRequest(FilterableRequestSpecification requestSpec, FilterContext filterContext) {
		this.requestSpec = requestSpec;
		this.filterContext = filterContext;
	}

	@Override
	public Values getHeaderValues() {
		return headersToValues(requestSpec.getHeaders());
	}

	@Override
	public String getContentType() {
		return requestSpec.getRequestContentType();
	}

	@Override
	public byte[] getContent() {
		return requestSpec.getBody();
	}

	@Override
	public String getRequestUrl(String baseUri) {
		System.out.println(filterContext.getCompleteRequestPath().replace(requestSpec.getBaseUri(), baseUri));
		return filterContext.getCompleteRequestPath().replace(requestSpec.getBaseUri(), baseUri);
//		
//		System.out.println("BaseUri"+baseUri);
//		System.out.println(requestSpec.getBasePath());
//		System.out.println(requestSpec.getBaseUri());
//		System.out.println("hahah"+filterContext.getRequestPath());
//		System.out.println("sadas"+filterContext.getCompleteRequestPath());
//		return baseUri == null ? filterContext.getCompleteRequestPath() : (baseUri + filterContext.getRequestPath());
		//return filterContext.getRequestPath();
		//return "http://nidi.guru/raml/v1/base/data?param=bu";
		//return baseUri;
		//return baseUri == null ? url : (baseUri + path);
		//return filterContext.getCompleteRequestPath();
	}

	@Override
	public String getMethod() {
		return filterContext.getRequestMethod().toString();
	}

	@Override
	public Values getQueryValues() {
		return mapToValues(requestSpec.getQueryParams());
	}

	@Override
	public Values getFormValues() {
		return mapToValues(requestSpec.getFormParams());
	}
}
