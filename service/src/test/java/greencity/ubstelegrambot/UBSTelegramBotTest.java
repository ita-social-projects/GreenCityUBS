package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    private final String botToken = "testToken";
    private final String botName = "testBot";

    @BeforeEach
    void setUp() {
        ubsTelegramBot = new UBSTelegramBot(botToken, botName, telegramService);
    }

    @Test
    void testGetBotUsername() {
        assertEquals(botName, ubsTelegramBot.getBotUsername());
    }

//    @Test
//    void testOnUpdateReceived_withTextMessage() {
//        Update update = mock(Update.class);
//        Message message = mock(Message.class);
//        when(update.hasMessage()).thenReturn(true);
//        when(update.getMessage()).thenReturn(message);
//        when(message.hasText()).thenReturn(true);
//
//        ubsTelegramBot.onUpdateReceived(update);
//
//        verify(telegramService, times(1)).processTextCommand(update);
//    }

//    @Test
//    void testOnUpdateReceived_withPhotoMessage() {
//        Update update = mock(Update.class);
//        Message message = mock(Message.class);
//        when(update.hasMessage()).thenReturn(true);
//        when(update.getMessage()).thenReturn(message);
//        when(message.hasPhoto()).thenReturn(true);
//
//        ubsTelegramBot.onUpdateReceived(update);
//
//        verify(telegramService, times(1)).processImageCommand(update);
//    }

//    @Test
//    void testOnUpdateReceived_withCallbackQuery() {
//        Update update = mock(Update.class);
//        CallbackQuery callbackQuery = mock(CallbackQuery.class);
//
//        when(update.hasCallbackQuery()).thenReturn(true);
//        lenient().when(update.getCallbackQuery()).thenReturn(callbackQuery);
//
//        ubsTelegramBot.onUpdateReceived(update);
//
//        verify(telegramService, times(1)).processCallBackQuery(update);
//    }

}
