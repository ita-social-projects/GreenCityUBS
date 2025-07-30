package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static greencity.ubstelegrambot.constant.TelegramConstants.GREEN_OFFICE_THANK_YOU_MESSAGE;
import static greencity.ubstelegrambot.constant.TelegramConstants.INVALID_EMAIL_MESSAGE;
import static greencity.ubstelegrambot.constant.TelegramConstants.UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramGreenOfficeServiceTest {
    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TelegramGreenOfficeServiceImpl telegramGreenOfficeService;

    @Test
    void processGreenOfficeEmail_shouldReturnInvalidEmailMessage_whenEmailIsInvalid() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setText("invalid-email");
        message.setChat(chat);
        message.setFrom(new User());

        SendMessage result = telegramGreenOfficeService.processGreenOfficeEmail(message);

        assertEquals(INVALID_EMAIL_MESSAGE, result.getText());
    }

    @Test
    void processGreenOfficeEmail_shouldReturnErrorMessage_whenChatNotFound() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setText("test@example.com");
        User user = new User();
        user.setId(12345L);
        message.setFrom(user);
        message.setChat(chat);

        when(telegramChatRepository.findByChatId("12345")).thenReturn(Optional.empty());

        SendMessage result = telegramGreenOfficeService.processGreenOfficeEmail(message);

        assertEquals(UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN, result.getText());
    }

    @Test
    void processGreenOfficeEmail_shouldSendNotification_withFullName_whenUserExists() {
        Chat mockChat = new Chat();
        mockChat.setId(123L);
        Message message = new Message();
        message.setText("user@example.com");
        User from = new User();
        from.setId(1L);
        message.setFrom(from);
        message.setChat(mockChat);

        greencity.entity.user.User userEntity = new greencity.entity.user.User();
        userEntity.setRecipientName("John");
        userEntity.setRecipientSurname("Doe");

        TelegramChat chat = new TelegramChat();
        chat.setUser(userEntity);
        chat.setChatState(ChatState.ENTERING_GREEN_OFFICE_EMAIL);

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramGreenOfficeService.processGreenOfficeEmail(message);

        verify(notificationService).notifyManagerWithNewGreenOfficeRequestFromTelegramBot("user@example.com",
            "John Doe");
        verify(telegramChatRepository).save(chat);
        assertEquals(GREEN_OFFICE_THANK_YOU_MESSAGE, result.getText());
        assertEquals(ChatState.NORMAL, chat.getChatState());
    }

    @Test
    void processGreenOfficeEmail_shouldSendNotification_withUsername_whenUserIsNull() {
        Chat mockChat = new Chat();
        mockChat.setId(123L);
        Message message = new Message();
        message.setText("user@example.com");
        User from = new User();
        from.setId(1L);
        from.setUserName("tg_user");
        message.setFrom(from);
        message.setChat(mockChat);

        TelegramChat chat = new TelegramChat();
        chat.setUser(null);
        chat.setChatState(ChatState.ENTERING_GREEN_OFFICE_EMAIL);

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramGreenOfficeService.processGreenOfficeEmail(message);

        verify(notificationService).notifyManagerWithNewGreenOfficeRequestFromTelegramBot("user@example.com",
            "tg_user");
        verify(telegramChatRepository).save(chat);
        assertEquals(GREEN_OFFICE_THANK_YOU_MESSAGE, result.getText());
        assertEquals(ChatState.NORMAL, chat.getChatState());
    }
}