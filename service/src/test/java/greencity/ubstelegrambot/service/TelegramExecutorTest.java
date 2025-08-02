package greencity.ubstelegrambot.service;

import greencity.exceptions.bots.MessageWasNotSent;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramExecutorTest {

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    private final TelegramExecutor telegramExecutor = new TelegramExecutor();

    @Test
    void executeCommandTest() throws Exception {
        BotApiMethod<?> botApiMethod = mock(BotApiMethod.class);
        telegramExecutor.executeCommand(ubsTelegramBot, botApiMethod);

        verify(ubsTelegramBot).execute(botApiMethod);
    }

    @Test
    void executeCommandTestCatchesException() throws Exception {
        BotApiMethod<?> botApiMethod = mock(BotApiMethod.class);
        when(ubsTelegramBot.execute(botApiMethod)).thenThrow(new TelegramApiException());

        assertThrows(
            MessageWasNotSent.class,
            () -> telegramExecutor.executeCommand(ubsTelegramBot, botApiMethod));
        verify(ubsTelegramBot).execute(botApiMethod);
    }

    @Test
    void executeGetFileTest() throws Exception {
        GetFile getFile = mock(GetFile.class);
        telegramExecutor.executeGetFile(ubsTelegramBot, getFile);

        verify(ubsTelegramBot).execute(getFile);
    }

    @Test
    void executeGetFileTestCatchesException() throws Exception {
        GetFile getFile = mock(GetFile.class);
        when(ubsTelegramBot.execute(getFile)).thenThrow(new TelegramApiException());

        assertThrows(
            MessageWasNotSent.class,
            () -> telegramExecutor.executeGetFile(ubsTelegramBot, getFile));
        verify(ubsTelegramBot).execute(getFile);
    }

    @Test
    void executeSendPhotoTest() throws Exception {
        SendPhoto sendPhoto = Mockito.mock(SendPhoto.class);
        telegramExecutor.executeSendPhoto(ubsTelegramBot, sendPhoto);

        verify(ubsTelegramBot).execute(sendPhoto);
    }

    @Test
    void executeSendPhotoTestCatchesException() throws Exception {
        SendPhoto sendPhoto = Mockito.mock(SendPhoto.class);
        when(ubsTelegramBot.execute(sendPhoto))
            .thenThrow(new TelegramApiException());

        assertThrows(
            MessageWasNotSent.class,
            () -> telegramExecutor.executeSendPhoto(ubsTelegramBot, sendPhoto));
        verify(ubsTelegramBot).execute(sendPhoto);
    }

    @Test
    void executeSendFileTest() throws Exception {
        SendDocument sendDocument = Mockito.mock(SendDocument.class);
        telegramExecutor.executeSendFile(ubsTelegramBot, sendDocument);

        verify(ubsTelegramBot).execute(sendDocument);
    }

    @Test
    void executeSendFileTestCatchesException() throws Exception {
        SendDocument sendDocument = Mockito.mock(SendDocument.class);
        when(ubsTelegramBot.execute(sendDocument))
                .thenThrow(new TelegramApiException());

        assertThrows(
                MessageWasNotSent.class,
                () -> telegramExecutor.executeSendFile(ubsTelegramBot, sendDocument));
        verify(ubsTelegramBot).execute(sendDocument);
    }
}
