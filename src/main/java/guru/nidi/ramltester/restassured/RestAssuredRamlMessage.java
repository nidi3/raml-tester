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

import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Headers;
import guru.nidi.ramltester.model.Values;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

class RestAssuredRamlMessage {
    Values headersToValues(Headers headers) {
        final Values headerValues = new Values();
        for (final Header header : headers) {
            headerValues.addValue(header.getName(), header.getValue());
        }
        return headerValues;
    }

    Values mapToValues(Map<String, ?> map) {
        final Values values = new Values();
        for (final Entry<String, ?> param : map.entrySet()) {
            if (param.getValue() instanceof Collection) {
                values.addValues(param.getKey(), (Collection<?>) param.getValue());
            } else {
                values.addValue(param.getKey(), param.getValue());
            }
        }
        return values;
    }
}
