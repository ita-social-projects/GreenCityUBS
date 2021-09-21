package greencity.exceptions;

public class NotificationNotFoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public NotificationNotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
