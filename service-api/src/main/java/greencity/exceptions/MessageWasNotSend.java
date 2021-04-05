package greencity.exceptions;

/**
 * Exception show that the message was not send to user.
 *
 * @author Pikhotskyi Vladyslav
 */
public class MessageWasNotSend extends RuntimeException {
    /**
     * Constructor.
     */
    public MessageWasNotSend(String message) {
        super(message);
    }
}
