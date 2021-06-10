package greencity.exceptions;

/**
 * Exception is thrown when status order is incorrect.
 */
public class BadOrderStatusRequestException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public BadOrderStatusRequestException(String message) {
        super(message);
    }
}