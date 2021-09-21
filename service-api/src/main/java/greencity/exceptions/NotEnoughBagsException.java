package greencity.exceptions;

/**
 * Exception is thrown when there are not enough big bags.
 *
 * @author Denys Kisliak
 */
public class NotEnoughBagsException extends RuntimeException {
    /**
     * Constructor for NotFoundException.
     *
     * @param message - giving message.
     */
    public NotEnoughBagsException(String message) {
        super(message);
    }
}
