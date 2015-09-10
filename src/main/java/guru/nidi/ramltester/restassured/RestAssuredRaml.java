package guru.nidi.ramltester.restassured;
import java.util.Map;
import java.util.Map.Entry;

import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Headers;

import guru.nidi.ramltester.model.Values;

abstract class RestAssuredRaml {

	protected Values headersToValues(Headers headers) {
		Values headerValues = new Values();
		for (Header header : headers) {
			headerValues.addValue(header.getName(), header.getValue());
		}
		return headerValues;
	}

	protected Values mapToValues(Map<String, ?> map) {
		Values values = new Values();
		for (Entry<String, ?> param : map.entrySet()) {
			values.addValue(param.getKey(), param.getValue());
		}
		return values;
	}
}
