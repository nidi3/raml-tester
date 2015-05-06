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
package guru.nidi.ramltester.servlet;

import guru.nidi.ramltester.core.RamlChecker;
import guru.nidi.ramltester.core.RamlReport;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class ServletTester {
    private final RamlChecker checker;

    public ServletTester(RamlChecker checker) {
        this.checker = checker;
    }

    public RamlReport testAgainst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        final ServletRamlRequest httpRequest = new ServletRamlRequest((HttpServletRequest) request);
        final ServletRamlResponse httpResponse = new ServletRamlResponse((HttpServletResponse) response);
        chain.doFilter(httpRequest, httpResponse);
        return checker.check(httpRequest, httpResponse);
    }
}
