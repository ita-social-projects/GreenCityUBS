package greencity.exceptions;

/**
 * Exception is shown when amount of big bags is not enough
 *
 * @author Denys Kisliak
 */
public class NotEnoughBagsException extends RuntimeException{
    /**
     * Constructor.
     */
    public NotEnoughBagsException(String message) {
        super(message);
    }
}
