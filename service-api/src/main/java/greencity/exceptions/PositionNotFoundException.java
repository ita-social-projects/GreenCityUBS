package greencity.exceptions;

/**
 * Exception that is thrown when position doesn't exist.
 */
public class PositionNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PositionNotFoundException(String message) {
        super(message);
    }
}
