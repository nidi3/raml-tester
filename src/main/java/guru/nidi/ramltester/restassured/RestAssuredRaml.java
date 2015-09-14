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
