package greencity.exceptions;

public class EventsNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EventsNotFoundException(String message) {
        super(message);
    }
}