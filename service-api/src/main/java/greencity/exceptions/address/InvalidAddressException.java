package greencity.exceptions.address;

import lombok.experimental.StandardException;

/**
 * Exception thrown when the provided address fails validation. This could occur
 * if the coordinates, city, or region do not match the expected values based on
 * external data (e.g., Google API). This exception typically results in a
 * response with a {@code 400 Bad Request} status when thrown in the validation
 * layer.
 */
@StandardException
public class InvalidAddressException extends RuntimeException {
}
