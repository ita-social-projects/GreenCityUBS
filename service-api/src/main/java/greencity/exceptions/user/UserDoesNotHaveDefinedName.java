package greencity.exceptions.user;

/**
 * Exception user does not have name or surname.
 *
 * @author Max Bohonko
 */
public class UserDoesNotHaveDefinedName extends RuntimeException {
    /**
     * Constructor.
     */
    public UserDoesNotHaveDefinedName(String message) {
        super(message);
    }
}
