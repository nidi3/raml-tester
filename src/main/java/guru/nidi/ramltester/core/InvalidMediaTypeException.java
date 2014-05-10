package guru.nidi.ramltester.core;

/**
 *
 */
class InvalidMediaTypeException extends RuntimeException {
    private final String mimeType;

    public InvalidMediaTypeException(String mimeType, String message) {
        super(message);
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
