package greencity.ubstelegrambot.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.enums.ChatState;
import greencity.enums.NotificationType;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private TelegramExecutor telegramExecutor;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    @Test
    void testSendNotification() {
        User user = User.builder().id(32L).recipientEmail("user@email.com")
            .telegramBot(
                new TelegramChat(1L, "123456", ChatState.NORMAL, Instant.now(), true, "username", "first_name",
                    "last_name", 0, null, null,
                    new ArrayList<>(), new ArrayList<>(), Instant.now()))
            .build();
        UserNotification notification = new UserNotification()
            .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
            .setId(42L)
            .setUser(user);
        NotificationDto notificationDto = NotificationDto.builder()
            .title("Test Title")
            .body("Test Body")
            .build();

        telegramNotificationService.sendNotification(notification, notificationDto);

        SendMessage expectedMessage = new SendMessage("123456", "Test Title\n\nTest Body");
        verify(telegramExecutor).executeCommand(expectedMessage);
        verifyNoMoreInteractions(telegramExecutor);
    }

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
