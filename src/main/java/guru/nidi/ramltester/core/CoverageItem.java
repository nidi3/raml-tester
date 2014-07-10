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
    };

    public abstract Set<String> get(RamlCoverage coverage);
}
