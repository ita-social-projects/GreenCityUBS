package greencity.exceptions;

/**
 * Exception that is thrown when receiving station doesn't exist.
 */
public class ReceivingStationNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public ReceivingStationNotFoundException(String message) {
        super(message);
    }
}
