package greencity.ubstelegrambot.service;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.language.LanguageVO;
import greencity.dto.user.UserVO;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.enums.NotificationType;
import greencity.repository.NotificationTemplateRepository;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;
    private final User user = User.builder().id(32L).recipientEmail("user@email.com")
        .telegramBot(new TelegramChat(1L, "12345", false, true, "username", "first_name", "last_name", null, new ArrayList<>()))
        .build();
    private final UserVO userVO = UserVO.builder().languageVO(LanguageVO.builder().code("ua").build()).build();
    private final UserNotification notification = new UserNotification()
        .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
        .setId(42L)
        .setUser(user);
    private final NotificationTemplate template = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

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

        userEntity.setTelegramBot(new TelegramChat(1L, "12345", false, true, "username", "first_name", "last_name", userEntity, new ArrayList<>()));
        assertTrue(telegramNotificationService.isEnabled(userEntity));

    }
}
