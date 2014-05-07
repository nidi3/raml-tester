package guru.nidi.ramltester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class UriComponents {
    private static final String HTTP_PATTERN = "(?i)(http|https):";

    private static final String USERINFO_PATTERN = "([^@/]*)";

    private static final String HOST_IPV4_PATTERN = "[^\\[/?#:]*";

    private static final String HOST_IPV6_PATTERN = "\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]";

    private static final String HOST_PATTERN = "(" + HOST_IPV6_PATTERN + "|" + HOST_IPV4_PATTERN + ")";

    private static final String PORT_PATTERN = "(\\d*)";

    private static final String PATH_PATTERN = "([^?#]*)";

    private static final String LAST_PATTERN = "(.*)";

    private static final Pattern HTTP_URL_PATTERN = Pattern.compile(
            "^" + HTTP_PATTERN + "(//(" + USERINFO_PATTERN + "@)?" + HOST_PATTERN + "(:" + PORT_PATTERN + ")?" + ")?" +
                    PATH_PATTERN + "(\\?" + LAST_PATTERN + ")?"
    );

    private final String scheme;
    private final String userInfo;
    private final String host;
    private final Integer port;
    private final String path;
    private final String query;

    private UriComponents(String scheme, String userInfo, String host, Integer port, String path, String query) {
        this.scheme = scheme;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
    }

    public static UriComponents fromHttpUrl(String httpUrl) {
        Matcher m = HTTP_URL_PATTERN.matcher(httpUrl);
        if (m.matches()) {
            String scheme = m.group(1);
            scheme = (scheme != null) ? scheme.toLowerCase() : null;
            String userInfo = m.group(4);
            String host = m.group(5);
            if (scheme != null && scheme.length() > 0 && (host == null || host.length() == 0)) {
                throw new IllegalArgumentException("[" + httpUrl + "] is not a valid HTTP URL");
            }
            Integer port = null;
            String portString = m.group(7);
            if (portString != null && portString.length() > 0) {
                port = Integer.parseInt(portString);
            }
            String path = m.group(8);
            String query = m.group(10);

            return new UriComponents(scheme, userInfo, host, port, path, query);
        } else {
            throw new IllegalArgumentException("[" + httpUrl + "] is not a valid HTTP URL");
        }
    }

    public String getScheme() {
        return scheme;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }
}
