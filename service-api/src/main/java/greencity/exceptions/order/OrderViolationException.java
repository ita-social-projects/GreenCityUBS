package greencity.exceptions.order;

/**
 * Exception is thrown when order doesn't exist.
 */
public class OrderViolationException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public OrderViolationException(String message) {
        super(message);
    }
}
