package greencity.exceptions;

public class UserNotFoundException extends RuntimeException {
    /**
     * Constructor.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
