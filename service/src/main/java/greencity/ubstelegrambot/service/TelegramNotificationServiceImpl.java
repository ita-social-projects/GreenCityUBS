package greencity.ubstelegrambot.service;

import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.TelegramManager;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramNotificationService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements TelegramNotificationService {
    private final ApplicationContext applicationContext;
    private final SimpMessagingTemplate messagingTemplate;
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramExecutor executor;

    @Override
    public void notifyNewMessage(TelegramMessageDto messageDto, Long chatId) {
        messagingTemplate.convertAndSend("/topic/messages/" + chatId, messageDto);
    }

    @Override
    public void notifyNewChat(ChatDto chatDto) {
        messagingTemplate.convertAndSend("/topic/chats", chatDto);
    }

    @Override
    public void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            SendMessage notification =
                MessageFactory.createNotificationMessageForManager(manager.getChatId(), username, messageText,
                    innerChatId);
            executor.executeCommand(telegramBot, notification);
        }
    }

    @Override
    public void notifyManagerAboutEndSupportModeFromUser(String username) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            var notification = MessageFactory.createEndSupportModeNotification(manager.getChatId(), username);
            executor.executeCommand(telegramBot, notification);
        }
    }
}
