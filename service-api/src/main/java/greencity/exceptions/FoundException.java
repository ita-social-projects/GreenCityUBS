package greencity.exceptions;

public class FoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public FoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public FoundException(String message) {
        super(message);
    }
}
