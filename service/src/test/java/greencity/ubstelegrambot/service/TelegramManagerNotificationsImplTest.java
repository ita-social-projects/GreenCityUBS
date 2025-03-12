package greencity.ubstelegrambot.service;

import greencity.entity.telegram.NotificationTimestamp;
import greencity.entity.telegram.PendingMessage;
import greencity.entity.telegram.TelegramManager;
import greencity.repository.NotificationTimestampRepository;
import greencity.repository.PendingMessageRepository;
import greencity.repository.TelegramManagerRepository;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramManagerNotificationsImplTest {

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TelegramExecutor telegramExecutor;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private PendingMessageRepository pendingMessageRepository;

    @Mock
    private NotificationTimestampRepository notificationTimestampRepository;

    @InjectMocks
    private TelegramManagerNotificationServiceImpl telegramManagerNotifications;

    private static final long NOTIFICATION_COOLDOWN_SECONDS = 60;

    private String chatId;
    private int messageCount;
    private PendingMessage pendingMessage;

    @BeforeEach
    void setUp() {
        chatId = "123";
        messageCount = 2;
        pendingMessage = new PendingMessage(chatId, messageCount);
    }

    @Test
    void shouldNotifyManagerTest_WhenPendingMessageIsNull_AndFindInstantByChatIdIsNull_AndMessageCountIsGreaterThanZero_AndPendingMessagesExceedCooldown() {
        Instant instantExceededCooldown = Instant.now().minusSeconds(NOTIFICATION_COOLDOWN_SECONDS);
        when(pendingMessageRepository.findByChatId(chatId))
            .thenReturn(null);
        when(pendingMessageRepository.save(any(PendingMessage.class)))
            .thenReturn(pendingMessage);
        when(notificationTimestampRepository.findInstantByChatId(chatId))
            .thenReturn(null, instantExceededCooldown);

        telegramManagerNotifications.shouldNotifyManager(chatId);

        verify(pendingMessageRepository, times(3)).save(any(PendingMessage.class));
        verify(notificationTimestampRepository, times(2)).save(any(NotificationTimestamp.class));
    }

    @Test
    void shouldNotifyManagerTest_WhenPendingMessageIsNotNull_AndFindInstantByChatIdIsNull_AndMessageCountIsGreaterThanZero_AndPendingMessagesExceedCooldown() {
        Instant instantExceededCooldown = Instant.now().minusSeconds(NOTIFICATION_COOLDOWN_SECONDS);

        when(pendingMessageRepository.findByChatId(chatId))
            .thenReturn(pendingMessage);
        when(notificationTimestampRepository.findInstantByChatId(chatId))
            .thenReturn(null, instantExceededCooldown);

        telegramManagerNotifications.shouldNotifyManager(chatId);

        verify(pendingMessageRepository, times(2)).save(any(PendingMessage.class));
        verify(notificationTimestampRepository, times(2)).save(any(NotificationTimestamp.class));
    }

    @Test
    void shouldNotifyManagerTest_WhenPendingMessageIsNotNull_AndFindInstantByChatIdIsNotNull_AndMessageCountIsGreaterThanZero_AndPendingMessagesExceedCooldown() {
        Instant instantExceededCooldown = Instant.now().minusSeconds(NOTIFICATION_COOLDOWN_SECONDS);

        when(pendingMessageRepository.findByChatId(chatId))
            .thenReturn(pendingMessage);
        when(notificationTimestampRepository.findInstantByChatId(chatId))
            .thenReturn(instantExceededCooldown, instantExceededCooldown);

        telegramManagerNotifications.shouldNotifyManager(chatId);

        verify(pendingMessageRepository, times(2)).save(any(PendingMessage.class));
        verify(notificationTimestampRepository).save(any(NotificationTimestamp.class));
    }

    @Test
    void shouldNotifyManagerTest_WhenPendingMessageIsNotNull_AndFindInstantByChatIdIsNotNull_AndMessageCountIsZero_AndPendingMessagesExceedCooldown() {
        Instant instantExceededCooldown = Instant.now().minusSeconds(NOTIFICATION_COOLDOWN_SECONDS);
        messageCount = 0;
        pendingMessage = new PendingMessage(chatId, messageCount);

        when(pendingMessageRepository.findByChatId(chatId))
            .thenReturn(pendingMessage);
        when(notificationTimestampRepository.findInstantByChatId(chatId))
            .thenReturn(instantExceededCooldown, instantExceededCooldown);

        telegramManagerNotifications.shouldNotifyManager(chatId);

        verify(pendingMessageRepository).save(any(PendingMessage.class));
        verify(notificationTimestampRepository, never()).save(any(NotificationTimestamp.class));
    }

    @Test
    void shouldNotifyManagerTest_WhenPendingMessageIsNotNull_AndFindInstantByChatIdIsNotNull_AndMessageCountIsGreaterThanZero_AndPendingMessagesDoNotExceedCooldown() {
        Instant instantThatDoesNotExceedCooldown = Instant.now();

        when(pendingMessageRepository.findByChatId(chatId))
            .thenReturn(pendingMessage);
        when(notificationTimestampRepository.findInstantByChatId(chatId))
            .thenReturn(instantThatDoesNotExceedCooldown);

        telegramManagerNotifications.shouldNotifyManager(chatId);

        verify(pendingMessageRepository).save(any(PendingMessage.class));
        verify(notificationTimestampRepository, never()).save(any(NotificationTimestamp.class));
    }

    @Test
    void checkPendingMessagesTestWhenPendingMessagesExceedCooldown() {
        List<PendingMessage> pendingMessages = List.of(
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount));
        int numberOfPendingMessages = pendingMessages.size();
        Instant instantExceededCooldown = Instant.now().minusSeconds(NOTIFICATION_COOLDOWN_SECONDS);
        Optional<NotificationTimestamp> notificationTimestampOptional = Optional.of(
            new NotificationTimestamp(chatId, instantExceededCooldown));

        when(pendingMessageRepository.findAll()).thenReturn(pendingMessages);
        when(notificationTimestampRepository.findByChatId(chatId))
            .thenReturn(notificationTimestampOptional);
        when(applicationContext.getBean(UBSTelegramBot.class))
            .thenReturn(ubsTelegramBot);

        telegramManagerNotifications.checkPendingMessages();

        verify(pendingMessageRepository).findAll();
        verify(notificationTimestampRepository, times(numberOfPendingMessages))
            .findByChatId(chatId);
        verify(notificationTimestampRepository, times(numberOfPendingMessages))
            .save(any(NotificationTimestamp.class));
        verify(applicationContext, times(numberOfPendingMessages))
            .getBean(UBSTelegramBot.class);
        verify(pendingMessageRepository, times(numberOfPendingMessages))
            .deleteByChatId(chatId);
    }

    @Test
    void checkPendingMessagesTestWhenPendingMessagesDidNotExceedCooldown() {
        List<PendingMessage> pendingMessages = List.of(
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount));
        int numberOfPendingMessages = pendingMessages.size();
        Instant instantThatDoesNotExceedCooldown = Instant.now();
        Optional<NotificationTimestamp> notificationTimestampOptional = Optional.of(
            new NotificationTimestamp(chatId, instantThatDoesNotExceedCooldown));

        when(pendingMessageRepository.findAll()).thenReturn(pendingMessages);
        when(notificationTimestampRepository.findByChatId(chatId))
            .thenReturn(notificationTimestampOptional);

        telegramManagerNotifications.checkPendingMessages();

        verify(pendingMessageRepository).findAll();
        verify(notificationTimestampRepository, times(numberOfPendingMessages))
            .findByChatId(chatId);
        verify(notificationTimestampRepository, never())
            .save(any(NotificationTimestamp.class));
        verify(applicationContext, never())
            .getBean(UBSTelegramBot.class);
        verify(pendingMessageRepository, never())
            .deleteByChatId(chatId);
    }

    @Test
    void checkPendingMessagesTestWhenMessageCountIsZero() {
        messageCount = 0;
        List<PendingMessage> pendingMessages = List.of(
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount),
            new PendingMessage(chatId, messageCount));
        int numberOfPendingMessages = pendingMessages.size();
        Instant instantThatDoesNotExceedCooldown = Instant.now();
        Optional<NotificationTimestamp> notificationTimestampOptional = Optional.of(
            new NotificationTimestamp(chatId, instantThatDoesNotExceedCooldown));

        when(pendingMessageRepository.findAll()).thenReturn(pendingMessages);
        when(notificationTimestampRepository.findByChatId(chatId))
            .thenReturn(notificationTimestampOptional);

        telegramManagerNotifications.checkPendingMessages();

        verify(pendingMessageRepository).findAll();
        verify(notificationTimestampRepository, times(numberOfPendingMessages))
            .findByChatId(chatId);
        verify(notificationTimestampRepository, never())
            .save(any(NotificationTimestamp.class));
        verify(applicationContext, never())
            .getBean(UBSTelegramBot.class);
        verify(pendingMessageRepository, never())
            .deleteByChatId(chatId);
    }

    @Test
    void sendSupportNotificationMessageToManagersTest() {
        List<TelegramManager> telegramManagersList = List.of(
            new TelegramManager(chatId, null),
            new TelegramManager(chatId, null));
        int amountOfTelegramManagers = telegramManagersList.size();
        SendMessage notification = new SendMessage();

        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            when(telegramManagerRepository.findAll())
                .thenReturn(telegramManagersList);
            mockedStatic
                .when(
                    () -> MessageFactory.createNotificationMessageForManager(eq(chatId), anyString(), eq(messageCount)))
                .thenReturn(notification);

            telegramManagerNotifications.sendSupportNotificationMessageToManagers(ubsTelegramBot, chatId, messageCount);

            verify(telegramManagerRepository).findAll();
            verify(telegramExecutor, times(amountOfTelegramManagers))
                .executeCommand(ubsTelegramBot, notification);
        }
    }

    @Test
    void notifyManagerAboutEndSupportModeFromUserTest() {
        List<TelegramManager> telegramManagersList = List.of(
            new TelegramManager(chatId, null),
            new TelegramManager(chatId, null));
        int amountOfTelegramManagers = telegramManagersList.size();
        SendMessage notification = new SendMessage();

        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            when(telegramManagerRepository.findAll())
                .thenReturn(telegramManagersList);
            mockedStatic.when(() -> MessageFactory.createEndSupportModeNotification(eq(chatId), anyString()))
                .thenReturn(notification);
            when(applicationContext.getBean(UBSTelegramBot.class))
                .thenReturn(ubsTelegramBot);

            telegramManagerNotifications.notifyManagerAboutEndSupportModeFromUser(chatId);

            verify(telegramManagerRepository).findAll();
            verify(applicationContext, times(amountOfTelegramManagers))
                .getBean(UBSTelegramBot.class);
            verify(telegramExecutor, times(amountOfTelegramManagers))
                .executeCommand(ubsTelegramBot, notification);
        }
    }
}
