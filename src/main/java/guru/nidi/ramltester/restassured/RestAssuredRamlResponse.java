package guru.nidi.ramltester.restassured;
import com.jayway.restassured.response.Response;

import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;

class RestAssuredRamlResponse extends RestAssuredRaml implements RamlResponse {

	private final Response response;

	RestAssuredRamlResponse(Response response) {
		this.response = response;
	}

	@Override
	public int getStatus() {
		return response.statusCode();
	}

	@Override
	public Values getHeaderValues() {
		return headersToValues(response.getHeaders());
	}

	@Override
	public String getContentType() {
		return response.getContentType();
	}

	@Override
	public byte[] getContent() {
		return response.getBody().asByteArray();
	}
}