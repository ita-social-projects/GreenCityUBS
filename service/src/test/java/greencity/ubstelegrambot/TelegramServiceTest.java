package greencity.ubstelegrambot;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.language.LanguageVO;
import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.repository.NotificationTemplateRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.Optional;
import static greencity.enums.NotificationReceiverType.MOBILE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @InjectMocks
    private TelegramService telegramService;
    private final User user = User.builder().id(32L).recipientEmail("user@email.com")
        .telegramBot(TelegramBot.builder().id(1L).chatId(1L).isNotify(true).build())
        .build();
    private final UserVO userVO = UserVO.builder().languageVO(LanguageVO.builder().code("ua").build()).build();
    private final UserNotification notification = new UserNotification()
        .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
        .setId(42L)
        .setUser(user);
    private final NotificationTemplate template = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

    @Test
    void testSendNotification() throws TelegramApiException {
        SendMessage sendMessage = new SendMessage(
            notification.getUser().getTelegramBot().getChatId().toString(),
            template.getTitle() + "\n\n" + template.getNotificationPlatforms().getFirst().getBody());
        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
                notification.getNotificationType(), MOBILE))
            .thenReturn(Optional.of(template));
        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
        when(ubsTelegramBot.execute(sendMessage)).thenReturn(null);

        telegramService.sendNotification(notification, MOBILE, 0L);
        verify(userRemoteClient).findNotDeactivatedByEmail(notification.getUser().getRecipientEmail());
        verify(ubsTelegramBot).execute(sendMessage);
    }

    @Test
    @SneakyThrows
    void testTelegramException() {
        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
                notification.getNotificationType(), MOBILE)).thenReturn(Optional.of(template));
        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
        when(ubsTelegramBot.execute(any(SendMessage.class))).thenThrow(new TelegramApiException());

        assertThrows(MessageWasNotSent.class, () -> telegramService.sendNotification(notification, MOBILE, 0L));
    }

    @Test
    void isEnabled() {
        assertFalse(telegramService.isEnabled(null));

        User newUser = new User();
        assertFalse(telegramService.isEnabled(newUser));

        newUser.setTelegramBot(new TelegramBot());
        assertFalse(telegramService.isEnabled(newUser));

        newUser.setTelegramBot(new TelegramBot(1L, 123L, true, newUser));
        assertTrue(telegramService.isEnabled(newUser));

        newUser.setTelegramBot(new TelegramBot(1L, 123L, false, newUser));
        assertFalse(telegramService.isEnabled(newUser));
    }
}
