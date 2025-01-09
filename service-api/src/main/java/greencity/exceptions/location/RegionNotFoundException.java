package greencity.exceptions.location;

/**
 * Exception that is thrown when region is not found.
 */
public class RegionNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public RegionNotFoundException(String message) {
        super(message);
    }
}
