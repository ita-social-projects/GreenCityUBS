package greencity.exceptions.user;

import lombok.experimental.StandardException;

/**
 * Exception is thrown when ubs_user doesn't exists.
 */
@StandardException
public class UBSuserNotFoundException extends RuntimeException {
}
