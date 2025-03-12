package greencity.ubstelegrambot.service;

import greencity.entity.telegram.NotificationTimestamp;
import greencity.entity.telegram.PendingMessage;
import greencity.entity.telegram.TelegramManager;
import greencity.repository.NotificationTimestampRepository;
import greencity.repository.PendingMessageRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramManagerNotificationService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramManagerNotificationServiceImpl implements TelegramManagerNotificationService {
    private final ApplicationContext applicationContext;
    private final TelegramExecutor telegramExecutor;
    private final TelegramManagerRepository telegramManagerRepository;
    private final NotificationTimestampRepository notificationTimestampRepository;
    private final PendingMessageRepository pendingMessageRepository;
    @Value("${telegram.manager.notification.period}")
    private Long notificationCooldownMilliseconds;

    @Override
    public void shouldNotifyManager(String chatId) {
        Instant now = Instant.now();
        var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
        PendingMessage pendingMessage = pendingMessageRepository.findByChatId(chatId);
        if (pendingMessage == null) {
            pendingMessage = pendingMessageRepository.save(
                PendingMessage.builder().chatId(chatId).messageCount(0).build());
        }

        int messageCount = pendingMessage.getMessageCount();
        pendingMessage.setMessageCount(messageCount + 1);
        pendingMessageRepository.save(pendingMessage);

        if (notificationTimestampRepository.findInstantByChatId(chatId) == null) {
            notificationTimestampRepository.save(NotificationTimestamp.builder()
                .chatId(chatId).lastNotificationTime(now).build());
            sendSupportNotificationMessageToManagers(telegramBot, chatId, 1);
        }
        if (messageCount > 0 && now.isAfter(notificationTimestampRepository
            .findInstantByChatId(chatId).plusMillis(notificationCooldownMilliseconds))) {
            notificationTimestampRepository.save(NotificationTimestamp.builder()
                .chatId(chatId).lastNotificationTime(now).build());

            pendingMessage.setMessageCount(0);
            pendingMessageRepository.save(pendingMessage);

            sendSupportNotificationMessageToManagers(telegramBot, chatId, messageCount);
        }
    }

    @Scheduled(fixedDelayString = "${telegram.manager.notification.period}")
    @Transactional
    @Override
    public void checkPendingMessages() {
        Instant now = Instant.now();
        List<PendingMessage> pendingMessages = pendingMessageRepository.findAll();

        for (PendingMessage pendingMessage : pendingMessages) {
            String chatId = pendingMessage.getChatId();
            int messageCount = pendingMessage.getMessageCount();

            Optional<NotificationTimestamp> lastNotification = notificationTimestampRepository.findByChatId(chatId);
            Instant lastTime = lastNotification.map(NotificationTimestamp::getLastNotificationTime)
                .orElse(Instant.MIN);

            if (messageCount > 0 && now.isAfter(lastTime.plusSeconds(notificationCooldownMilliseconds))) {
                notificationTimestampRepository.save(NotificationTimestamp.builder()
                    .chatId(chatId)
                    .lastNotificationTime(now)
                    .build());

                var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
                sendSupportNotificationMessageToManagers(telegramBot, chatId, messageCount);
                pendingMessageRepository.deleteByChatId(chatId);
            }
        }
    }

    @Override
    public void sendSupportNotificationMessageToManagers(TelegramLongPollingBot telegramLongPollingBot, String chatId,
        int messageCount) {
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            var notification =
                MessageFactory.createNotificationMessageForManager(chatId, manager.getChatId(), messageCount);
            telegramExecutor.executeCommand(telegramLongPollingBot, notification);
        }
    }

    @Override
    public void notifyManagerAboutEndSupportModeFromUser(String chatId) {
        List<TelegramManager> telegramManagers = telegramManagerRepository.findAll();
        for (TelegramManager manager : telegramManagers) {
            var notification = MessageFactory.createEndSupportModeNotification(chatId, manager.getChatId());
            var telegramBot = applicationContext.getBean(UBSTelegramBot.class);
            telegramExecutor.executeCommand(telegramBot, notification);
        }
    }
}
