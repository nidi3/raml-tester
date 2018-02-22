/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.restassured3;

import guru.nidi.ramltester.core.*;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class RamlValidationFilter implements Filter {
    private final RamlChecker ramlChecker;
    private final ReportStore reportStore;

    public RamlValidationFilter(RamlChecker ramlChecker) {
        this.ramlChecker = ramlChecker;
        this.reportStore = new ThreadLocalReportStore();
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
                           FilterContext filterContext) {
        final Response response = filterContext.next(requestSpec, responseSpec);
        final RamlReport report = ramlChecker.check(new RestAssuredRamlRequest(requestSpec), new RestAssuredRamlResponse(response));
        reportStore.storeReport(report);
        return response;

    }

    public ReportStore getReportStore() {
        return reportStore;
    }
}
