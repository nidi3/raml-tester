package guru.nidi.ramltester.core;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
class Usage {
    private String path;
    private Set<String> requestHeaders = Collections.emptySet();
    private Set<String> responseHeaders = Collections.emptySet();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Set<String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Set<String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Set<String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    @Override
    public String toString() {
        return "Usage{" +
                "path='" + path + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", responseHeaders=" + responseHeaders +
                '}';
    }
}
