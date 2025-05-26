package greencity.ubstelegrambot.service;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
import greencity.enums.NotificationType;
import greencity.repository.NotificationTemplateRepository;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        .telegramBot(new AuthorizedUser("12345", false, false, null, false))
        .build();
    private final UserNotification notification = new UserNotification()
        .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
        .setId(42L)
        .setUser(user);
    private final NotificationTemplate template = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

    @Test
    void isEnabled() {
        assertFalse(telegramNotificationService.isEnabled(null));

        User userEntity = new User();
        assertFalse(telegramNotificationService.isEnabled(userEntity));

        userEntity.setTelegramBot(new AuthorizedUser());
        assertFalse(telegramNotificationService.isEnabled(userEntity));

        userEntity.setTelegramBot(new AuthorizedUser("12345", false, true, userEntity, false));
        assertTrue(telegramNotificationService.isEnabled(userEntity));

    }
}
