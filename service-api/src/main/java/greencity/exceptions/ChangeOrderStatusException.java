package greencity.exceptions;

/**
 * Exception that user enters unexisting bag.
 *
 * @author Liubomyr Pater
 */
public class ChangeOrderStatusException extends RuntimeException {
    /**
     * Constructor.
     */
    public ChangeOrderStatusException(String message) {
        super(message);
    }
}