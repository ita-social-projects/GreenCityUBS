package greencity.exceptions;

/**
 * Exception, that is throw if user try to save status that's already
 * exist in database.
 */
public class LocationStatusAlreadyExistException extends RuntimeException{
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public LocationStatusAlreadyExistException(String message) {
        super(message);
    }
}
