package greencity.exceptions.location;

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
