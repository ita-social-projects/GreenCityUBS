package greencity.exceptions.user;

public class UserNotFoundException extends RuntimeException {
    /**
     * Constructor.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
