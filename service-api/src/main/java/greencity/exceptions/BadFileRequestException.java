package greencity.exceptions;

public class BadFileRequestException extends RuntimeException {
    /**
     * Default constructor.
     */
    public BadFileRequestException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public BadFileRequestException(String message) {
        super(message);
    }
}
