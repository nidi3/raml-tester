package guru.nidi.ramltester.core;

import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 *
 */
class MediaType {
    private static final String WILDCARD_TYPE = "*";

    private String type;
    private String subtype;
    private Map<String, String> parameters;

    public MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    public static MediaType valueOf(String mimeType) {
        if (mimeType == null || mimeType.length() == 0) {
            throw new InvalidMediaTypeException(mimeType, "'mimeType' must not be empty");
        }
        String[] parts = tokenizeToStringArray(mimeType, ";");

        String fullType = parts[0].trim();
        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new InvalidMediaTypeException(mimeType, "does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new InvalidMediaTypeException(mimeType, "does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1, fullType.length());
        if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
            throw new InvalidMediaTypeException(mimeType, "wildcard type is legal only in '*/*' (all mime types)");
        }

        Map<String, String> parameters = null;
        if (parts.length > 1) {
            parameters = new LinkedHashMap<>(parts.length - 1);
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i];
                int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    String attribute = parameter.substring(0, eqIndex);
                    String value = parameter.substring(eqIndex + 1, parameter.length());
                    parameters.put(attribute, value);
                }
            }
        }

        try {
            return new MediaType(type, subtype, parameters);
        } catch (UnsupportedCharsetException ex) {
            throw new InvalidMediaTypeException(mimeType, "unsupported charset '" + ex.getCharsetName() + "'");
        } catch (IllegalArgumentException ex) {
            throw new InvalidMediaTypeException(mimeType, ex.getMessage());
        }
    }

    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(getType());
    }

    public boolean isWildcardSubtype() {
        return WILDCARD_TYPE.equals(getSubtype()) || getSubtype().startsWith("*+");
    }

    public boolean isCompatibleWith(MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType() || other.isWildcardType()) {
            return true;
        } else if (getType().equals(other.getType())) {
            if (getSubtype().equals(other.getSubtype())) {
                return true;
            }
            // wildcard with suffix? e.g. application/*+xml
            if (this.isWildcardSubtype() || other.isWildcardSubtype()) {

                int thisPlusIdx = getSubtype().indexOf('+');
                int otherPlusIdx = other.getSubtype().indexOf('+');

                if (thisPlusIdx == -1 && otherPlusIdx == -1) {
                    return true;
                } else if (thisPlusIdx != -1 && otherPlusIdx != -1) {
                    String thisSubtypeNoSuffix = getSubtype().substring(0, thisPlusIdx);
                    String otherSubtypeNoSuffix = other.getSubtype().substring(0, otherPlusIdx);

                    String thisSubtypeSuffix = getSubtype().substring(thisPlusIdx + 1);
                    String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);

                    if (thisSubtypeSuffix.equals(otherSubtypeSuffix) &&
                            (WILDCARD_TYPE.equals(thisSubtypeNoSuffix) || WILDCARD_TYPE.equals(otherSubtypeNoSuffix))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getType() {
        return this.type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    private static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
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
}
