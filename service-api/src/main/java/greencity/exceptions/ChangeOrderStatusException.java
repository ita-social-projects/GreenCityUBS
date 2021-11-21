package greencity.exceptions;

/**
 * Exception that user enters unexisting bag.
 *
 * @author Marian Diakiv
 */
public class ChangeOrderStatusException extends RuntimeException{
    /**
     * Constructor.
     */
    public ChangeOrderStatusException(String message) {
        super(message);
    }
}