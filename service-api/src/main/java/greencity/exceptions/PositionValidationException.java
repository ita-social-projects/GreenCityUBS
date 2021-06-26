package greencity.exceptions;

/**
 * Exception that is thrown when position is invalid.
 */
public class PositionValidationException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PositionValidationException(String message) {
        super(message);
    }
}
