package greencity.exceptions;

/**
 * Exception that is thrown when when there are no violations in the order.
 */
public class ViolationNotFoundException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public ViolationNotFoundException(String message) {
        super(message);
    }
}
