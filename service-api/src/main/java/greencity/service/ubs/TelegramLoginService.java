package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramLoginService {
    void logoutManager(String chatId);

    SendMessage processInputManagerCredentialsRequest(Message message);
}
