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
package guru.nidi.ramltester.loader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 */
public class SimpleUrlFetcher implements UrlFetcher {
    private static final String HTTP_DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss 'GMT'";

    @Override
    public InputStream fetchFromUrl(CloseableHttpClient client, String base, String name, long ifModifiedSince) throws IOException {
        final String suffix = (name == null || name.length() == 0) ? "" : ("/" + encodeUrl(name));
        final HttpGet get = postProcessGet(new HttpGet(base + suffix));
        if (ifModifiedSince > 0) {
            get.addHeader("if-modified-since", httpDate(ifModifiedSince));
        }
        final CloseableHttpResponse getResult = client.execute(get);
        switch (getResult.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                return getResult.getEntity().getContent();
            case HttpStatus.SC_NOT_MODIFIED:
                EntityUtils.consume(getResult.getEntity());
                return null;
            default:
                throw new IOException("Http response status not ok: " + getResult.getStatusLine().toString());
        }
    }

    protected String encodeUrl(String name) {
        return name.replace(" ", "%20");
    }

    protected HttpGet postProcessGet(HttpGet get) {
        return get;
    }

    private String httpDate(long date) {
        final SimpleDateFormat format = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }
}
