package greencity.exceptions;

public class NotFoundOrderAddressException extends RuntimeException {
    /**
     * Default constructor.
     */
    public NotFoundOrderAddressException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public NotFoundOrderAddressException(String message) {
        super(message);
    }
}
