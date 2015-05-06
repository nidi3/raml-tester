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

/**
 *
 */
public final class UriComponents {
    private static final class Pattern {
        private static final String
                HTTP = "(?i)(http|https):",
                USERINFO = "([^@/]*)",
                HOST_IPV4 = "[^\\[/?#:]*",
                HOST_IPV6 = "\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]",
                HOST = "(" + HOST_IPV6 + "|" + HOST_IPV4 + ")",
                PORT = "(\\d*)",
                PATH = "([^?#]*)",
                LAST = "(.*)";

        public static final java.util.regex.Pattern
                HTTP_URL = java.util.regex.Pattern.compile("^" + HTTP + "(//(" + USERINFO + "@)?" + HOST + "(:" + PORT + ")?" + ")?" + PATH + "(\\?" + LAST + ")?"),
                QUERY_PARAM = java.util.regex.Pattern.compile("([^&=]+)(=?)([^&]+)?");
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
        Matcher m = Pattern.HTTP_URL.matcher(httpUrl);
        if (m.matches()) {
            String scheme = m.group(1);
            scheme = scheme == null ? null : scheme.toLowerCase();
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

    public static Values parseQuery(String query) {
        final Values q = new Values();
        if (query != null) {
            final Matcher m = Pattern.QUERY_PARAM.matcher(query);
            while (m.find()) {
                final String name = m.group(1);
                final String eq = m.group(2);
                final String value = m.group(3);
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
