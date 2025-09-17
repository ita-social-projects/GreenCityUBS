package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramManager;
import greencity.enums.MessageType;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramBotResponseService;
import greencity.service.ubs.TelegramLanguageService;
import greencity.service.ubs.TelegramNotificationService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationServiceImpl implements TelegramNotificationService {
    private final TelegramManagerRepository telegramManagerRepository;
    private final TelegramLanguageService telegramLanguageService;
    private final TelegramExecutor telegramExecutor;
    private final TelegramBotResponseService telegramBotResponseService;

    @Override
    public void notifyManagerAboutNewMessagesFromUser(String username, String messageText, Long innerChatId) {
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            String lang = telegramLanguageService.getChatLanguage(manager.getChatId());
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.CLIENT_WANT_TO_SPEAK);
            SendMessage notification =
                MessageFactory.createNotificationMessageForManager(manager.getChatId(), username, messageText,
                    innerChatId, text);

            notifyManagerSafely(manager, notification);
        }
    }

    @Override
    public void notifyManagerAboutEndSupportModeFromUser(String username) {
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            String lang = telegramLanguageService.getChatLanguage(manager.getChatId());
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.CLIENT_END_SUPPORT_NOTIFICATION);
            var notification = MessageFactory.createEndSupportModeNotification(
                manager.getChatId(), username, text);
            notifyManagerSafely(manager, notification);
        }
    }

    private void notifyManagerSafely(TelegramManager telegramManager, SendMessage notification) {
        try {
            telegramExecutor.executeCommand(notification);
        } catch (TelegramBotExecutionException e) {
            log.warn("Failed to notify manager chatId={}", telegramManager.getChatId(), e);
        }
    }
}
