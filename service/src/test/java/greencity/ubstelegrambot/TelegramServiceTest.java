package greencity.ubstelegrambot;

import greencity.client.OutOfRequestRestClient;
import greencity.dto.LanguageVO;
import greencity.dto.NotificationDto;
import greencity.dto.UserVO;
import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.MessageWasNotSend;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.NotificationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramServiceTest {

    @Mock
    private OutOfRequestRestClient restClient;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @InjectMocks
    private TelegramService telegramService;

    private final UserNotification notification = new UserNotification();
    private final User user = User.builder().id(32L).recipientEmail("user@email.com")
        .telegramBot(TelegramBot.builder().id(1L).chatId(1L).build())
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
            .findNotificationTemplateByNotificationTypeAndLanguageCode(notification.getNotificationType(),
                "en")).thenReturn(Optional.of(template));

        when(restClient.findUserByEmail(notification.getUser().getRecipientEmail()))
            .thenReturn(Optional.of(userVO));
    }

    @Test
    public void testSendNotification() throws TelegramApiException {
        prepareTest();
        NotificationDto notificationDto = NotificationServiceImpl
            .createNotificationDto(notification, userVO.getLanguageVO().getCode(), templateRepository);

        SendMessage sendMessage = new SendMessage(
            notification.getUser().getTelegramBot().getChatId().toString(),
            notificationDto.getTitle() + "\n\n" + notificationDto.getBody());

        when(ubsTelegramBot.execute(sendMessage)).thenReturn(null);

        telegramService.sendNotification(notification);

        verify(restClient).findUserByEmail(notification.getUser().getRecipientEmail());
        verify(ubsTelegramBot).execute(sendMessage);
    }

    @Test(expected = MessageWasNotSend.class)
    public void testTelegramException() throws TelegramApiException {
        prepareTest();
        when(ubsTelegramBot.execute(any(SendMessage.class))).thenThrow(new TelegramApiException());
        telegramService.sendNotification(notification);
    }
}
