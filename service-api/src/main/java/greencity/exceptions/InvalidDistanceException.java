package greencity.exceptions;

/**
 * Exception that user enters invalid search radius for coordinates
 * clusterization.
 *
 * @author Oleh Bilonizhka
 */
public class InvalidDistanceException extends RuntimeException {
    /**
     * Constructor.
     */
    public InvalidDistanceException(String message) {
        super(message);
    }
}
