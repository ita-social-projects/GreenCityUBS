package greencity.exceptions.bots;

import lombok.experimental.StandardException;

/**
 * Exception show that the user already has connected to Telegram bot.
 *
 * @author Pikhotskyi Vladyslav
 */
@StandardException
public class TelegramBotAlreadyConnected extends RuntimeException {
}
