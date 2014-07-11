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
package guru.nidi.ramltester.core;

import java.util.Set;

/**
 *
 */
public enum CoverageItem {
    PATH {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedPaths();
        }
    },
    QUERY_PARAMETER {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedQueryParameters();
        }
    },
    FORM_PARAMETER {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedFormParameters();
        }
    },
    REQUEST_HEADER {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedRequestHeaders();
        }
    },
    RESPONSE_HEADER {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedResponseHeaders();
        }
    },
    RESPONSE_CODE {
        @Override
        public Set<String> get(RamlCoverage coverage) {
            return coverage.getUnusedResponseCodes();
        }
    };

    public abstract Set<String> get(RamlCoverage coverage);
}
