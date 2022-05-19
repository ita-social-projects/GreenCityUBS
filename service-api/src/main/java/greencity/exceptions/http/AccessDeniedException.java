package greencity.exceptions.http;

public class AccessDeniedException extends RuntimeException {
    /**
     * Default constructor.
     */
    public AccessDeniedException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
