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
package guru.nidi.ramltester.jaxrs;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

class CheckingClientFilter implements ClientRequestFilter, ClientResponseFilter {
    private final CheckingWebTarget target;

    public CheckingClientFilter(CheckingWebTarget target) {
        this.target = target;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (!(requestContext.getEntityStream() instanceof SavingOutputStream)) {
            requestContext.setEntityStream(new SavingOutputStream(requestContext.getEntityStream()));
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        target.check(new JaxrsContextRamlRequest(requestContext), new JaxrsContextRamlResponse(responseContext));
    }
}
