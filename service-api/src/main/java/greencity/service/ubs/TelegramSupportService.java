package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramSupportService {
    /**
     * Processes an incoming support message from a Telegram user.
     *
     * @param message the incoming Telegram {@link Message} containing user text,
     *                photos, or command.
     * @return a {@link SendMessage} object to be sent back to the user via
     *         Telegram, confirming that the message was received or an error
     *         occurred.
     */
    SendMessage processSupportMessage(Message message, String lang);

    /**
     * Processes an incoming support message from a Telegram user.
     *
     * @param edited the incoming Telegram {@link Message} containing user text,
     *                photos, or command.
     * @param lang {@link String} the current language
     * @return a {@link SendMessage} object to be sent back to the user via
     *         Telegram, confirming that the message was received or an error
     *         occurred.
     */
    SendMessage processEditedSupportMessage(Message edited, String lang);
}
