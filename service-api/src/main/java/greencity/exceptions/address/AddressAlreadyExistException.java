package greencity.exceptions.address;

/**
 * Exception informs user that address for delivery already exists.
 *
 * @author Ihor Volianskyi
 */
public class AddressAlreadyExistException extends RuntimeException {
    /**
     * Default constructor.
     */
    public AddressAlreadyExistException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public AddressAlreadyExistException(String message) {
        super(message);
    }
}
