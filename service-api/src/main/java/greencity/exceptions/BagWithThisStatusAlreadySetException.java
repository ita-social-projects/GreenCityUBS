package greencity.exceptions;

/**
 * Exception that thrown when status had been already set.
 */
public class BagWithThisStatusAlreadySetException extends RuntimeException {
    /**
     * Constructor.
     */
    public BagWithThisStatusAlreadySetException(String message) {
        super(message);
    }
}
