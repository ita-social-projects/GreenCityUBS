package greencity.service.ubs;

import greencity.dto.telegram.TelegramMessageDto;

public interface TelegramNotificationService {
    void notifyNewMessage(TelegramMessageDto messageDto, Long chatId);

    void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId);

    void notifyManagerAboutEndSupportModeFromUser(String username);
}
