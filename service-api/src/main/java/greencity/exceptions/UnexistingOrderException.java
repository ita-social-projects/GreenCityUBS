package greencity.exceptions;

public class UnexistingOrderException extends RuntimeException {
    /**
     * Default constructor.
     */
    public UnexistingOrderException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public UnexistingOrderException(String message) {
        super(message);
    }
}
