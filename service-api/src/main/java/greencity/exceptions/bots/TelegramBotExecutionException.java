package greencity.exceptions.bots;

import lombok.experimental.StandardException;

/**
 * Exception which is thrown when some operations with Telegram bot were not
 * successful.
 *
 * @author Pikhotskyi Vladyslav
 */
@StandardException
public class TelegramBotExecutionException extends RuntimeException {
}
