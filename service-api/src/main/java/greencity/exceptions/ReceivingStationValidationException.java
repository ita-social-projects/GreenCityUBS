package greencity.exceptions;

/**
 * Exception that is thrown when receiving station is invalid.
 */
public class ReceivingStationValidationException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public ReceivingStationValidationException(String message) {
        super(message);
    }
}
