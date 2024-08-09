package greencity.exceptions.http;

import lombok.experimental.StandardException;

/**
 * Exception thrown when remote server did not respond.
 */
@StandardException
public class RemoteServerUnavailableException extends RuntimeException {
}
