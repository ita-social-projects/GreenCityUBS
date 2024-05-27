package greencity.service.notification;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.notification.EmailNotificationDto;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @InjectMocks
    private EmailService emailService;
    @Mock
    private UserRemoteClient userRemoteClient;

    @Test
    void isEnabled() {
        assertFalse(emailService.isEnabled(null));

        User user = new User();
        assertFalse(emailService.isEnabled(user));

        user.setRecipientEmail(null);
        assertFalse(emailService.isEnabled(user));

        user.setRecipientEmail("user@email.com");
        assertTrue(emailService.isEnabled(user));
    }

    @Test
    void sendNotification() {
        UserNotification notification = ModelUtils.TEST_USER_NOTIFICATION;
        NotificationDto notificationDto = ModelUtils.TEST_NOTIFICATION_DTO;

        EmailNotificationDto emailNotificationDto = EmailNotificationDto.builder()
            .email(notification.getUser().getRecipientEmail())
            .subject(notificationDto.getTitle())
            .message(notificationDto.getBody())
            .build();

        doNothing().when(userRemoteClient)
            .sendEmailNotification(emailNotificationDto);

        emailService.sendNotification(notification, notificationDto);

        verify(userRemoteClient).sendEmailNotification(emailNotificationDto);
    }
}
