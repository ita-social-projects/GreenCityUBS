package greencity.exceptions;

/**
 * Exception is thrown when blob doesn't exist.
 */
public class BlobNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public BlobNotFoundException(String message) {
        super(message);
    }
}
