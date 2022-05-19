package greencity.exceptions.location;

/**
 * Exception that throw if location was created early.
 */
public class LocationAlreadyCreatedException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public LocationAlreadyCreatedException(String message) {
        super(message);
    }
}
