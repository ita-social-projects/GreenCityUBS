package greencity.exceptions;

/**
 * Exception show that the user already has connected to telegram bot.
 *
 * @author Pikhotskyi Vladyslav
 */
public class TelegramBotAlreadyConnected extends RuntimeException {
    /**
     * Constructor.
     */
    public TelegramBotAlreadyConnected(String message) {
        super(message);
    }
}
