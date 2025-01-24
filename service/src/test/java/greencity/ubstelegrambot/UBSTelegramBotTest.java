package greencity.ubstelegrambot;

import greencity.constant.AppConstant;
import greencity.service.ubs.TelegramService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {
    final Long tgUserId = 12345L;
    @Mock
    private TelegramService telegramService;
    @Mock
    private TelegramExecutor executor;
    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    @Test
    void getBotUsernameTest() {
        assertNull(ubsTelegramBot.getBotUsername());
    }

    @Test
    void getBotTokenTest() {
        assertNull(ubsTelegramBot.getBotToken());
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndUnknownTelegramUserTest() {
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND);
        doNothing().when(telegramService).handleUnknownTelegramUser(update);
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService, times(1)).handleUnknownTelegramUser(update);
        verify(executor, times(1)).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndAuthorizedUserTest() {
        String uuId = UUID.randomUUID().toString();
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND + " " + uuId);
        doNothing().when(telegramService).handleAuthorizedUser(uuId, tgUserId);
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService, times(1)).handleAuthorizedUser(uuId, tgUserId);
        verify(executor, times(1)).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));
    }

    @Test
    public void onUpdateReceivedWithoutCommandTest() {
        Update update = createUpdate(tgUserId, "");

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService, times(0)).handleUnknownTelegramUser(update);
        verify(executor, times(0)).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

    }

    private Update createUpdate(Long tgUserId, String messageText) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(tgUserId);
        User from = new User();
        from.setId(tgUserId);
        message.setFrom(from);
        message.setText(messageText);
        message.setChat(chat);
        update.setMessage(message);

        return update;
    }
}
