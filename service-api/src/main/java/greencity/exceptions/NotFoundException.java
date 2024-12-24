package greencity.exceptions;

public class NotFoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public NotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
