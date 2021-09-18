package greencity.ubsviberbot;

import greencity.client.OutOfRequestRestClient;
import greencity.client.RestClient;
import greencity.dto.LanguageVO;
import greencity.dto.NotificationDto;
import greencity.dto.UserVO;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.enums.MessageType;
import greencity.entity.enums.NotificationReceiverType;
import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.exceptions.MessageWasNotSend;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.NotificationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static greencity.entity.enums.NotificationReceiverType.OTHER;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViberServiceImplTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private OutOfRequestRestClient outOfRequestRestClient;

    @Mock
    private RestClient restClient;

    @Mock
    UserRepository userRepository;

    @Mock
    ViberBotRepository viberBotRepository;

    @InjectMocks
    private ViberServiceImpl viberService;

    private final UserNotification notification = new UserNotification();
    private final User user = User.builder().id(32L).recipientEmail("user@email.com")
        .viberBot(ViberBot.builder().id(1L).chatId("1").build())
        .build();
    private final UserVO userVO = UserVO.builder().languageVO(LanguageVO.builder().code("en").build()).build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        notification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
        notification.setId(42L);
        notification.setUser(user);
    }

    @BeforeEach
    public void prepareTest() {
        NotificationTemplate template = new NotificationTemplate();
        template.setTitle("Title");
        template.setBody("Body");

        when(templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(),
                "en", OTHER)).thenReturn(Optional.of(template));

        when(outOfRequestRestClient.findUserByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
    }

    @Test
    public void testSendWelcomeMessageAndPreRegisterViberBotForUser() {
        prepareTest();
        User unauthorizedUser = user;
        unauthorizedUser.setViberBot(null);
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(unauthorizedUser));
        when(viberBotRepository.save(any())).thenReturn(user.getViberBot());
        viberService.sendWelcomeMessageAndPreRegisterViberBotForUser("42", "32L");
        verify(viberBotRepository, times(1)).save(any());
        verify(userRepository, times(1)).findUserByUuid(anyString());
    }

    @Test
    public void testSendMessageAndRegisterViberBotForUser() {
        prepareTest();
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
    public void testSendNotification() {
        prepareTest();
        NotificationDto notificationDto = NotificationServiceImpl
            .createNotificationDto(notification, userVO.getLanguageVO().getCode(), OTHER, templateRepository);

        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(notification.getUser().getViberBot().getChatId())
            .type(MessageType.text)
            .text(notificationDto.getTitle() + "\n\n" + notificationDto.getBody())
            .build();

        when(restClient.sendMessage(sendMessageToUserDto)).thenReturn(null);

        viberService.sendNotification(notification);

        verify(outOfRequestRestClient).findUserByEmail(notification.getUser().getRecipientEmail());
        verify(restClient).sendMessage(any());
    }

    @Test(expected = MessageWasNotSend.class)
    public void testViberException() {
        prepareTest();
        when(restClient.sendMessage(any())).thenThrow(new RuntimeException());
        viberService.sendNotification(notification);
    }
}
