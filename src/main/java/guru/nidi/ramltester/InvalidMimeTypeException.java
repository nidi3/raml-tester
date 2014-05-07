package guru.nidi.ramltester;

/**
 *
 */
class InvalidMimeTypeException extends RuntimeException {
    private final String mimeType;
    public InvalidMimeTypeException(String mimeType,String message) {
        super(message);
        this.mimeType= mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
