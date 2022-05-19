package greencity.exceptions.location;

public class LocationNotFoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public LocationNotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public LocationNotFoundException(String message) {
        super(message);
    }
}
