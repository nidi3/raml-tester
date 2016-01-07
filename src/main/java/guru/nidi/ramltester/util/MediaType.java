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


import java.util.*;

/**
 *
 */
public final class MediaType {
    public static final Comparator<MediaType> QUALITY_COMPARATOR = new Comparator<MediaType>() {
        @Override
        public int compare(MediaType m1, MediaType m2) {
            return Double.compare(m2.getQualityParameter(), m1.getQualityParameter());
        }
    };

    public static final MediaType
            JSON = valueOf("application/json"),
            FORM_URL_ENCODED = valueOf("application/x-www-form-urlencoded"),
            MULTIPART = valueOf("multipart/form-data");

    private static final String CHARSET = "charset";
    private static final String WILDCARD_TYPE = "*";
    private static final Map<String, MediaType> KNOWN_SUFFICES = new HashMap<>();

    static {
        KNOWN_SUFFICES.put("json", JSON);
    }

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;

    private MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    public static MediaType valueOf(String mimeType) {
        if (mimeType == null || mimeType.length() == 0) {
            throw new InvalidMediaTypeException(mimeType, new Message("mediaType.empty"));
        }
        final String[] parts = tokenizeToStringArray(mimeType, ";");

        String fullType = parts[0].trim();
        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        final int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new InvalidMediaTypeException(mimeType, new Message("mediaType.noSlash"));
        }
        if (subIndex == fullType.length() - 1) {
            throw new InvalidMediaTypeException(mimeType, new Message("mediaType.noSubtype"));
        }
        final String type = fullType.substring(0, subIndex);
        final String subtype = fullType.substring(subIndex + 1, fullType.length());
        if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
            throw new InvalidMediaTypeException(mimeType, new Message("mediaType.wildcard.illegal"));
        }

        final Map<String, String> parameters = parseParameters(parts);

        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            checkParameter(mimeType, entry.getKey(), entry.getValue());
        }

        return new MediaType(type, subtype, parameters);
    }

    private static Map<String, String> parseParameters(String[] parts) {
        final Map<String, String> parameters = new LinkedHashMap<>(parts.length);
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                final String parameter = parts[i];
                final int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    final String attribute = parameter.substring(0, eqIndex);
                    final String value = parameter.substring(eqIndex + 1, parameter.length());
                    parameters.put(attribute, value);
                }
            }
        }
        return parameters;
    }

    public double getQualityParameter() {
        final String q = getParameter("q");
        return q == null ? 1 : Double.parseDouble(q);
    }

    private static void checkParameter(String mimeType, String key, String value) {
        if ("q".equals(key)) {
            final String unquoted = unquote(value);
            try {
                final double d = Double.parseDouble(unquoted);
                if (d < 0 || d > 1) {
                    throw new InvalidMediaTypeException(mimeType, new Message("mediaType.quality.illegal", unquoted));
                }
            } catch (NumberFormatException e) {
                throw new InvalidMediaTypeException(mimeType, new Message("mediaType.quality.illegal", unquoted));
            }
        }
    }

    private static boolean isQuotedString(String s) {
        return s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")));
    }

    private static String unquote(String s) {
        if (s == null) {
            return null;
        }
        return isQuotedString(s) ? s.substring(1, s.length() - 1) : s;
    }

    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(getType());
    }

    public boolean isWildcardSubtype() {
        return WILDCARD_TYPE.equals(getSubtype()) || getSubtype().startsWith("*+");
    }

    public int similarity(MediaType other) {
        if (getType().equals(other.getType())) {
            if (getSubtype().equals(other.getSubtype())) {
                return getParameters().equals(other.getParameters()) ? 4 : 3;
            }
            if (isThisOrOtherWildcardSubtype(other)) {
                return hasSameSuffix(other) ? 2 : 0;
            }
        }
        if (isWildcardType() || other.isWildcardType()) {
            return 1;
        }
        final MediaType thisKnown = applyKnownSuffices();
        final MediaType otherKnown = other.applyKnownSuffices();
        if (thisKnown != this || otherKnown != other) {
            return thisKnown.similarity(otherKnown);
        }
        return 0;
    }

    public boolean isCompatibleWith(MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType() || other.isWildcardType()) {
            return true;
        }
        final MediaType thisKnown = applyKnownSuffices();
        final MediaType otherKnown = other.applyKnownSuffices();
        if ((thisKnown != this || otherKnown != other) && thisKnown.isCompatibleWith(otherKnown)) {
            return true;
        }
        if (!getType().equals(other.getType())) {
            return false;
        }
        if (getSubtype().equals(other.getSubtype())) {
            return true;
        }
        // wildcard with suffix? e.g. application/*+xml
        return isThisOrOtherWildcardSubtype(other) && hasSameSuffix(other);
    }

    private boolean isThisOrOtherWildcardSubtype(MediaType other) {
        return isWildcardSubtype() || other.isWildcardSubtype();
    }

    private boolean hasSameSuffix(MediaType other) {
        final String thisSuffix = findSuffix();
        final String otherSuffix = other.findSuffix();
        return (thisSuffix == null && otherSuffix == null) || (thisSuffix != null && thisSuffix.equals(otherSuffix));
    }

    private MediaType applyKnownSuffices() {
        final MediaType known = KNOWN_SUFFICES.get(findSuffix());
        return known == null ? this : known;
    }

    private String findSuffix() {
        final int pos = getSubtype().indexOf('+');
        return pos == -1 ? null : getSubtype().substring(pos + 1);
    }

    public String getType() {
        return this.type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getCharset(String defaultCharset) {
        final String charset = parameters.get(CHARSET);
        return charset == null ? defaultCharset : charset;
    }

    private static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        final StringTokenizer st = new StringTokenizer(str, delimiters);
        final List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token = token.trim();
            if (token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    private static String[] toStringArray(Collection<String> collection) {
        return collection == null ? null : collection.toArray(new String[collection.size()]);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaType)) {
            return false;
        }
        final MediaType otherType = (MediaType) other;
        return (this.type.equalsIgnoreCase(otherType.type) &&
                this.subtype.equalsIgnoreCase(otherType.subtype) &&
                this.parameters.equals(otherType.parameters));
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.subtype.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String res = type + "/" + subtype;
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            res += ";" + entry.getKey() + "=" + entry.getValue();
        }
        return res;
    }
}
