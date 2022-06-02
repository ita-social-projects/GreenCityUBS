package greencity.exceptions;

public class BadRequestException extends RuntimeException {
    /**
     * Default constructor.
     */
    public BadRequestException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
