package greencity.exceptions.api;

import lombok.experimental.StandardException;

/**
 * Exception could be thrown if application have issues to connecting to Google
 * servers.
 */
@StandardException
public class GoogleApiException extends RuntimeException {
}
