package greencity.exceptions;

public class UnprocessableEntityException extends RuntimeException {
    /**
     * Default constructor.
     */
    public UnprocessableEntityException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
