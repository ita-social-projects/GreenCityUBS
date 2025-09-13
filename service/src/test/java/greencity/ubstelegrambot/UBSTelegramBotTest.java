package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    @Test
    void onUpdateReceivedTest() {
        Update update = mock(Update.class);

        doNothing().when(telegramService).processUpdate(update);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).processUpdate(update);
    }

    @Test
    void getBotUsernameTest() {
        String botName = "testBotName";
        String botToken = "testBotToken";
        ubsTelegramBot = new UBSTelegramBot(botToken, botName, telegramService);
        assertNotNull(ubsTelegramBot, "Instance UBSTelegramBot don't have to be null after creating.");

        String returnedBotName = ubsTelegramBot.getBotUsername();

        assertEquals(botName, returnedBotName, "The getBotUsername() should return the botName provided.");
    }
}
