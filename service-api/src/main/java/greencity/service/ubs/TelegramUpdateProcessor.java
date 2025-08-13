package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUpdateProcessor {
    /**
     * Processes both callback queries (e.g., logout actions) and standard messages,
     * delegating them to the appropriate handler based on the update type.
     *
     * @param update the incoming Telegram {@link Update} from the bot
     * @return a {@link SendMessage} with the appropriate response
     */
    SendMessage process(Update update);
}
