package greencity.exceptions;

/**
 * Exception that user enters unexisting bag.
 *
 * @author Marian Diakiv
 */
public class BagNotFoundException extends RuntimeException {
    /**
     * Constructor.
     */
    public BagNotFoundException(String message) {
        super(message);
    }
}
