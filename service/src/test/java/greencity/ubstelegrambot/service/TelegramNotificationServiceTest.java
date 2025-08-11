package greencity.ubstelegrambot.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.enums.ChatState;
import greencity.enums.NotificationType;
import greencity.repository.NotificationTemplateRepository;
import greencity.ubstelegrambot.UBSTelegramBot;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    private final User user = User.builder().id(32L).recipientEmail("user@email.com")
        .telegramBot(
            new TelegramChat(1L, "12345", ChatState.NORMAL, Instant.now(), true, "username", "first_name",
                "last_name", 0, null, null,
                new ArrayList<>(), new ArrayList<>(), Instant.now()))
        .build();
    private final UserNotification notification = new UserNotification()
        .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
        .setId(42L)
        .setUser(user);
    private final NotificationTemplate template = ModelUtils.TEST_NOTIFICATION_TEMPLATE;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    private NotificationTemplateRepository templateRepository;
    @Mock
    private UBSTelegramBot ubsTelegramBot;
    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

//    @Test
//    void testSendNotification() throws TelegramApiException {
//        SendMessage sendMessage = new SendMessage(
//            notification.getUser().getTelegramBot().getChatId(),
//            template.getTitle() + "\n\n" + template.getNotificationPlatforms().get(0).getBody());
//        when(templateRepository
//            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
//                notification.getNotificationType(), MOBILE))
//            .thenReturn(Optional.of(template));
//        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
//            .thenReturn(Optional.of(userVO));
//        when(ubsTelegramBot.execute(sendMessage)).thenReturn(null);
//
//        telegramNotificationService.sendNotification(notification, MOBILE, 0L);
//        verify(userRemoteClient).findNotDeactivatedByEmail(notification.getUser().getRecipientEmail());
//        verify(ubsTelegramBot).execute(sendMessage);
//    }
//
//    @Test
//    @SneakyThrows
//    void testTelegramException() {
//        when(templateRepository
//            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
//                notification.getNotificationType(), MOBILE)).thenReturn(Optional.of(template));
//        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
//            .thenReturn(Optional.of(userVO));
//        when(ubsTelegramBot.execute(any(SendMessage.class))).thenThrow(new TelegramApiException());
//
//        assertThrows(MessageWasNotSent.class,
//            () -> telegramNotificationService.sendNotification(notification, MOBILE, 0L));
//    }

    @Test
    void isEnabled() {
        assertFalse(telegramNotificationService.isEnabled(null));

        User userEntity = new User();
        assertFalse(telegramNotificationService.isEnabled(userEntity));

        userEntity.setTelegramBot(new TelegramChat());
        assertFalse(telegramNotificationService.isEnabled(userEntity));

        userEntity
            .setTelegramBot(new TelegramChat(1L, "12345", ChatState.NORMAL, Instant.now(), true, "username",
                "first_name", "last_name", 0,
                userEntity, null, new ArrayList<>(), new ArrayList<>(), Instant.now()));

        assertTrue(telegramNotificationService.isEnabled(userEntity));

    }
}
