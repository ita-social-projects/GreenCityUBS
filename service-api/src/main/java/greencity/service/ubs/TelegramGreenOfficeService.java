package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramGreenOfficeService {
    /**
     * Processes the user's email input for a Green Office request via Telegram.
     *
     * @param message the Telegram {@link Message} containing the email address from
     *                the user
     *
     * @return a {@link SendMessage} containing either an error or a thank-you
     *         response
     */
    SendMessage processGreenOfficeEmail(Message message, String lang);
}
