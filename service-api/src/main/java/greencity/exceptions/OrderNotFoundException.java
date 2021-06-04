package greencity.exceptions;

/**
 * Exception is thrown when order doesn't exist.
 */
public class OrderNotFoundException extends RuntimeException{

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public OrderNotFoundException(String message) {
        super(message);
    }
}
