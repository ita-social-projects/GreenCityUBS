package greencity.exceptions.user;

/**
 * Exception user is not owner of the order.
 *
 * @author Olena Sotnik
 */
public class UserIsNotOrderOwnerException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public UserIsNotOrderOwnerException(String message) {
        super(message);
    }
}
