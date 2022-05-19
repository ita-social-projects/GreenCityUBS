package greencity.exceptions.address;

public class AddressNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message - giving message.
     */
    public AddressNotFoundException(String message) {
        super(message);
    }
}
