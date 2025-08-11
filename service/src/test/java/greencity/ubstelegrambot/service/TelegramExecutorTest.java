package greencity.ubstelegrambot.service;

import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.ubstelegrambot.UBSTelegramBot;
import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramExecutorTest {

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @InjectMocks
    private TelegramExecutor telegramExecutor;

    @Test
    void executeCommandTest() throws Exception {
        BotApiMethod<?> botApiMethod = mock(BotApiMethod.class);
        Serializable serializable = mock(Serializable.class);

        doReturn(serializable).when(ubsTelegramBot).execute(any(BotApiMethod.class));

        telegramExecutor.executeCommand(botApiMethod);

        verify(ubsTelegramBot).execute(botApiMethod);
    }

    @Test
    void executeGetFileTest() throws Exception {
        GetFile getFile = mock(GetFile.class);
        File file = mock(File.class);

        doReturn(file).when(ubsTelegramBot).execute(any(GetFile.class));

        telegramExecutor.executeGetFile(getFile);

        verify(ubsTelegramBot).execute(getFile);
    }

    @Test
    void executeSendPhotoTest() throws Exception {
        SendPhoto sendPhoto = Mockito.mock(SendPhoto.class);
        Message message = mock(Message.class);

        doReturn(message).when(ubsTelegramBot).execute(any(SendPhoto.class));

        telegramExecutor.executeSendPhoto(sendPhoto);

        verify(ubsTelegramBot).execute(sendPhoto);
    }

    @Test
    void executeSendDocumentTest() throws Exception {
        SendDocument sendDocument = Mockito.mock(SendDocument.class);
        Message message = mock(Message.class);

        doReturn(message).when(ubsTelegramBot).execute(any(SendDocument.class));

        telegramExecutor.executeSendFile(sendDocument);

        verify(ubsTelegramBot).execute(sendDocument);
    }

    @Test
    void allExecutesTestCatchesException() throws Exception {
        BotApiMethod<?> botApiMethod = mock(BotApiMethod.class);
        GetFile getFile = mock(GetFile.class);
        SendPhoto sendPhoto = Mockito.mock(SendPhoto.class);
        SendDocument sendDocument = Mockito.mock(SendDocument.class);

        TelegramApiException exception = new TelegramApiException();
        doThrow(exception).when(ubsTelegramBot).execute(any(BotApiMethod.class));
        doThrow(exception).when(ubsTelegramBot).execute(any(SendPhoto.class));
        doThrow(exception).when(ubsTelegramBot).execute(any(SendDocument.class));

        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeCommand(botApiMethod));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeGetFile(getFile));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeSendPhoto(sendPhoto));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeSendFile(sendDocument));

        verify(ubsTelegramBot).execute(botApiMethod);
        verify(ubsTelegramBot).execute(getFile);
        verify(ubsTelegramBot).execute(sendPhoto);
        verify(ubsTelegramBot).execute(sendDocument);
    }

    @Test
    void allExecutesTestCatchesNull() throws Exception {
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeCommand(null));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeGetFile(null));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeSendPhoto(null));
        assertThrows(
            TelegramBotExecutionException.class,
            () -> telegramExecutor.executeSendFile(null));

        verify(ubsTelegramBot, never()).execute(any(BotApiMethod.class));
        verify(ubsTelegramBot, never()).execute(any(GetFile.class));
        verify(ubsTelegramBot, never()).execute(any(SendDocument.class));
        verify(ubsTelegramBot, never()).execute(any(SendPhoto.class));
    }
}
