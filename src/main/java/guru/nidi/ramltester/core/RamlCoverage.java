package guru.nidi.ramltester.core;

import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RamlCoverage {
    private final Set<String> unusedPaths;
    private final Set<String> unusedRequestHeaders;
    private final Set<String> unusedResponseHeaders;

    public RamlCoverage(Raml raml, List<RamlReport> reports) {
        unusedPaths = pathsOf(raml.getResources());
        unusedRequestHeaders = requestHeadersOf(raml.getResources());
        unusedResponseHeaders = responseHeadersOf(raml.getResources());
        for (RamlReport report : reports) {
            addUsage(report);
        }
    }

    private Set<String> requestHeadersOf(Map<String, Resource> resources) {
        Set<String> headers = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                headers.addAll(action.getHeaders().keySet());
            }
            headers.addAll(requestHeadersOf(entry.getValue().getResources()));
        }
        return headers;
    }

    private Set<String> responseHeadersOf(Map<String, Resource> resources) {
        Set<String> headers = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            for (Action action : entry.getValue().getActions().values()) {
                for (Response response : action.getResponses().values()) {
                    headers.addAll(response.getHeaders().keySet());
                }
            }
            headers.addAll(responseHeadersOf(entry.getValue().getResources()));
        }
        return headers;
    }

    private Set<String> pathsOf(Map<String, Resource> resources) {
        Set<String> paths = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            paths.add(entry.getValue().getUri());
            paths.addAll(pathsOf(entry.getValue().getResources()));
        }
        return paths;
    }

    private void addUsage(RamlReport report) {
        final Usage usage = report.getUsage();
        unusedPaths.remove(usage.getPath());
        unusedRequestHeaders.removeAll(usage.getRequestHeaders());
        unusedResponseHeaders.removeAll(usage.getResponseHeaders());
    }

    public Set<String> getUnusedPaths() {
        return unusedPaths;
    }

    public Set<String> getUnusedRequestHeaders() {
        return unusedRequestHeaders;
    }

    public Set<String> getUnusedResponseHeaders() {
        return unusedResponseHeaders;
    }

    @Override
    public String toString() {
        return "RamlCoverage{" +
                "unusedPaths=" + unusedPaths +
                ", unusedRequestHeaders=" + unusedRequestHeaders +
                ", unusedResponseHeaders=" + unusedResponseHeaders +
                '}';
    }
}
