package greencity.exceptions.location;

/**
 * Exception noticing that address id does not match area corresponding location
 * id .
 *
 * @author Olena Sotnik
 */
public class AddressNotWithinLocationAreaException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public AddressNotWithinLocationAreaException(String message) {
        super(message);
    }
}
