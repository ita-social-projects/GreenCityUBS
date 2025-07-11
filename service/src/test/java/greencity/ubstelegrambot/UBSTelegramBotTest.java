package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    private final String botName = "testBot";

    @BeforeEach
    void setUp() {
        String botToken = "testToken";
        ubsTelegramBot = new UBSTelegramBot(botToken, botName, telegramService);
    }

    @Test
    void testGetBotUsername() {
        assertEquals(botName, ubsTelegramBot.getBotUsername());
    }

    @Test
    void testOnUpdateReceived_withTextMessage() {
        Update update = mock(Update.class);
        ubsTelegramBot.onUpdateReceived(update);
        verify(telegramService, times(1)).processUpdate(update);
    }
}
