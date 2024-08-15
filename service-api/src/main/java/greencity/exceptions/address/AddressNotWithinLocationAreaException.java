package greencity.exceptions.address;

import lombok.experimental.StandardException;

/**
 * Exception noticing that address id does not match area corresponding location
 * id .
 *
 * @author Olena Sotnik
 */
@StandardException
public class AddressNotWithinLocationAreaException extends RuntimeException {
}
