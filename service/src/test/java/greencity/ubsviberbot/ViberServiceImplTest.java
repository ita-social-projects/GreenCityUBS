package greencity.ubsviberbot;

import greencity.client.UserRemoteClient;
import greencity.client.ViberClient;
import greencity.dto.language.LanguageVO;
import greencity.dto.user.UserVO;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.enums.MessageType;
import greencity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static greencity.enums.NotificationReceiverType.OTHER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViberServiceImplTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private UserRemoteClient userRemoteClient;;

    @Mock
    private ViberClient viberClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViberBotRepository viberBotRepository;

    @InjectMocks
    private ViberServiceImpl viberService;

    private final User user = User.builder()
        .id(32L)
        .recipientEmail("user@email.com")
        .viberBot(ViberBot.builder()
            .id(1L)
            .chatId("1")
            .isNotify(true)
            .build())
        .build();
    private final UserNotification notification = new UserNotification()
        .setNotificationType(NotificationType.LETS_STAY_CONNECTED)
        .setId(42L)
        .setUser(user);
    private final UserVO userVO = UserVO.builder()
        .languageVO(LanguageVO.builder()
            .code("en")
            .build())
        .build();
    private final NotificationTemplate template = new NotificationTemplate()
        .setTitle("Title")
        .setBody("Body");

    @Test
    void testSendWelcomeMessageAndPreRegisterViberBotForUser() {
        User unauthorizedUser = user;
        unauthorizedUser.setViberBot(null);
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(unauthorizedUser));
        when(viberBotRepository.save(any())).thenReturn(user.getViberBot());
        viberService.sendWelcomeMessageAndPreRegisterViberBotForUser("42", "32L");
        verify(viberBotRepository, times(1)).save(any());
        verify(userRepository, times(1)).findUserByUuid(anyString());
    }

    @Test
    void testSendMessageAndRegisterViberBotForUser() {
        User noBotUser = user;
        noBotUser.setViberBot(ViberBot.builder().id(1L).chatId("1").isNotify(false).build());
        when(viberBotRepository.findViberBotByChatId(anyString())).thenReturn(Optional.of(noBotUser.getViberBot()));
        when(viberBotRepository.save(any())).thenReturn(user.getViberBot());
        viberService.sendMessageAndRegisterViberBotForUser("1");
        viberService.sendMessageAndRegisterViberBotForUser("2");
        verify(viberBotRepository, times(1)).save(any());
        verify(viberBotRepository, times(2)).findViberBotByChatId(any());
    }

    @Test
    void testSendNotification() {
        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(notification.getUser().getViberBot().getChatId())
            .type(MessageType.text)
            .text(template.getTitle() + "\n\n" + template.getBody())
            .build();

        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(), userVO.getLanguageVO().getCode(), OTHER))
                    .thenReturn(Optional.of(template));
        when(viberClient.sendMessage(sendMessageToUserDto)).thenReturn(null);

        viberService.sendNotification(notification, 0L);

        verify(viberClient).sendMessage(any());
    }

    @Test
    void sendNotificationNotEnabled() {
        notification.getUser().getViberBot().setIsNotify(false);

        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(), userVO.getLanguageVO().getCode(), OTHER))
                    .thenReturn(Optional.of(template));

        viberService.sendNotification(notification, 0L);

        verify(viberClient, never()).sendMessage(any());
    }

    @Test
    void sendNotificationUserNotFoundException() {
        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> viberService.sendNotification(notification, 0L));
    }

    @Test
    void testViberException() {
        when(userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(), userVO.getLanguageVO().getCode(), OTHER))
                    .thenReturn(Optional.of(template));
        when(viberClient.sendMessage(any())).thenThrow(new RuntimeException());

        assertThrows(MessageWasNotSent.class, () -> viberService.sendNotification(notification, 0L));
    }

    @Test
    void setWebHook() {
        viberService.setWebhook();
        verify(viberClient).updateWebHook(any());
    }

    @Test
    void removeWebHook() {
        viberService.removeWebHook();
        verify(viberClient).updateWebHook(any());
    }

    @Test
    void getAccountInfo() {
        viberService.getAccountInfo();
        verify(viberClient).getAccountInfo();
    }

    @Test
    void init() {
        ReflectionTestUtils.setField(viberService, "viberBotUrl", "https://right.webhook.com");

        ResponseEntity<String> noWebhookResponse = ResponseEntity.ok().body("{}");
        when(viberClient.getAccountInfo()).thenReturn(noWebhookResponse);
        viberService.init();

        ResponseEntity<String> wrongWebhookResponse = ResponseEntity.ok().body("{\n" +
            "\"webhook\":\"https://wrong.webhook.com\"\n" +
            "}");
        when(viberClient.getAccountInfo()).thenReturn(wrongWebhookResponse);
        viberService.init();

        ResponseEntity<String> rightWebhookResponse = ResponseEntity.ok().body("{\n" +
            "\"webhook\":\"https://right.webhook.com\"\n" +
            "}");
        when(viberClient.getAccountInfo()).thenReturn(rightWebhookResponse);
        viberService.init();

        verify(viberClient, times(2)).updateWebHook(any());
    }

    @Test
    void isEnabled() {
        assertFalse(viberService.isEnabled(null));

        User user = new User();
        assertFalse(viberService.isEnabled(user));

        user.setViberBot(new ViberBot(1L, "123", false, user));
        assertFalse(viberService.isEnabled(user));

        user.getViberBot().setIsNotify(true);
        assertTrue(viberService.isEnabled(user));
    }
}
