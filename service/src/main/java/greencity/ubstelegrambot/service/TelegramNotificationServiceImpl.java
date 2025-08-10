package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramManager;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramLanguageService;
import greencity.service.ubs.TelegramNotificationService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements TelegramNotificationService {
    private final ApplicationContext applicationContext;
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramExecutor executor;
    private final TelegramLanguageService telegramLanguageService;

    public void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            String lang = telegramLanguageService.getChatLanguage(manager.getChatId());
            SendMessage notification =
                MessageFactory.createNotificationMessageForManager(manager.getChatId(), username, messageText,
                    innerChatId, lang);
            executor.executeCommand(telegramBot, notification);
        }
    }

    @Override
    public void notifyManagerAboutEndSupportModeFromUser(String username) {
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            String lang = telegramLanguageService.getChatLanguage(manager.getChatId());
            var notification = MessageFactory.createEndSupportModeNotification(manager.getChatId(), username, lang);
            executor.executeCommand(telegramBot, notification);
        }
    }
}
