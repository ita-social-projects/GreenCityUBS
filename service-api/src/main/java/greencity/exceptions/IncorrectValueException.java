package greencity.exceptions;

/**
 * Exception that user enters invalid search radius for coordinates
 * clusterization.
 *
 * @author Oleh Bilonizhka
 */
public class IncorrectValueException extends RuntimeException {
    /**
     * Constructor.
     */
    public IncorrectValueException(String message) {
        super(message);
    }
}
