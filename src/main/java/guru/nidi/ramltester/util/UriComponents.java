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
package guru.nidi.ramltester.util;

import guru.nidi.ramltester.model.Values;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class UriComponents {
    private static final class HttpUrl {
        private static final String
                HTTP = "(?i)(http|https):",
                USERINFO = "([^@/]*)",
                HOST_IPV4 = "[^\\[/?#:]*",
                HOST_IPV6 = "\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]",
                HOST = "(" + HOST_IPV6 + "|" + HOST_IPV4 + ")",
                PORT = "(\\d*)",
                PATH = "([^?#]*)",
                LAST = "(.*)";

        public static final Pattern
                PATTERN = Pattern.compile("^" + HTTP + "(//(" + USERINFO + "@)?" + HOST + "(:" + PORT + ")?" + ")?" + PATH + "(\\?" + LAST + ")?");

        public static final int
                GROUP_SCHEME = 1,
                GROUP_USER = 4,
                GROUP_HOST = 5,
                GROUP_PORT = 7,
                GROUP_PATH = 8,
                GROUP_QUERY = 10;
    }

    private static final class QueryParam {
        public static final Pattern PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

        public static final int
                GROUP_NAME = 1,
                GROUP_EQUAL = 2,
                GROUP_VALUE = 3;
    }

    private final String scheme;
    private final String userInfo;
    private final String host;
    private final Integer port;
    private final String path;
    private final String queryString;
    private final Values queryParameters;

    private UriComponents(String scheme, String userInfo, String host, Integer port, String path, String query) {
        this.scheme = scheme;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.queryString = query;
        this.queryParameters = parseQuery(query);
    }

    public static UriComponents fromHttpUrl(String httpUrl) {
        final Matcher m = HttpUrl.PATTERN.matcher(httpUrl);
        if (m.matches()) {
            final String scheme = m.group(HttpUrl.GROUP_SCHEME) == null ? null : m.group(HttpUrl.GROUP_SCHEME).toLowerCase();
            final String userInfo = m.group(HttpUrl.GROUP_USER);
            final String host = m.group(HttpUrl.GROUP_HOST);
            if (scheme != null && scheme.length() > 0 && (host == null || host.length() == 0)) {
                throw new IllegalArgumentException("[" + httpUrl + "] is not a valid HTTP URL");
            }
            final String portString = m.group(HttpUrl.GROUP_PORT);
            final Integer port = (portString != null && portString.length() > 0)
                    ? Integer.parseInt(portString)
                    : null;
            final String path = m.group(HttpUrl.GROUP_PATH);
            final String query = m.group(HttpUrl.GROUP_QUERY);

            return new UriComponents(scheme, userInfo, host, port, path, query);
        } else {
            throw new IllegalArgumentException("[" + httpUrl + "] is not a valid HTTP URL");
        }
    }

    public static Values parseQuery(String query) {
        final Values q = new Values();
        if (query != null) {
            final Matcher m = QueryParam.PATTERN.matcher(query);
            while (m.find()) {
                final String name = m.group(QueryParam.GROUP_NAME);
                final String eq = m.group(QueryParam.GROUP_EQUAL);
                final String value = m.group(QueryParam.GROUP_VALUE);
                final String emptyValue = eq != null && eq.length() > 0 ? "" : null;
                q.addValue(name, value == null ? emptyValue : value);
            }
        }
        return q;
    }

    public String getServer() {
        return (scheme == null ? "" : (scheme + "://")) +
                (userInfo == null ? "" : (userInfo + "@")) +
                (host == null ? "" : host) +
                (port == null ? "" : (":" + port));
    }

    public String getUri() {
        return getServer() + (path == null ? "" : path);
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

    public String getQueryString() {
        return queryString;
    }

    public Values getQueryParameters() {
        return queryParameters;
    }
}
