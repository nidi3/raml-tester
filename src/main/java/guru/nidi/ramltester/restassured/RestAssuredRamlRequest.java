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

import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;

class RestAssuredRamlRequest extends RestAssuredRamlMessage implements RamlRequest {
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
        String body = requestSpec.getBody();
        return body == null ? null : body.getBytes();
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        return baseUri == null || baseUri.length() == 0
                ? filterContext.getCompleteRequestPath()
                : filterContext.getCompleteRequestPath().replace(requestSpec.getBaseUri(), baseUri);
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
