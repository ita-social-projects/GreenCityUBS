package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.service.TelegramExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    @Mock
    private TelegramUpdateProcessor processor;

    @Mock
    private TelegramExecutor executor;

    @Mock
    private ApplicationContext applicationContext;

    private final String botName = "testBot";

    @Test
    void onUpdateReceived_shouldCallExecutorWithCorrectMessage() {
        // given
        Long chatId = 12345L;
        String expectedText = "Hello from processor!";

        var telegramUser = new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(chatId);

        Message message = new Message();
        message.setFrom(telegramUser);
        message.setText("/start");

        Update update = new Update();
        update.setMessage(message);

        SendMessage expectedSendMessage = new SendMessage(chatId.toString(), expectedText);

        // mocks
        when(telegramService.processUpdate(update)).thenReturn(processor);
        when(processor.process(update)).thenReturn(expectedSendMessage);
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(ubsTelegramBot);

        // when
        ubsTelegramBot.onUpdateReceived(update);

        // then
        verify(executor).executeCommand(ubsTelegramBot, expectedSendMessage);
    }
//    @BeforeEach
//    void setUp() {
//        String botToken = "testToken";
//        ubsTelegramBot = new UBSTelegramBot(botToken, botName, telegramService);
//    }
//
//    @Test
//    void testGetBotUsername() {
//        assertEquals(botName, ubsTelegramBot.getBotUsername());
//    }
//
//    @Test
//    void testOnUpdateReceived_withTextMessage() {
//        Update update = mock(Update.class);
//        ubsTelegramBot.onUpdateReceived(update);
//        verify(telegramService, times(1)).processUpdate(update);
//    }
}
