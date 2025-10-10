package greencity.exceptions.user;

import lombok.experimental.StandardException;

/**
 * Exception thrown when there is an issue with changing status of a user.
 */
@StandardException
public class UserStatusUpdateException extends RuntimeException {
}
