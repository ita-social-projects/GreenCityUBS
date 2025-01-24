package greencity.ubstelegrambot;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.UserRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {
    final Long tgUserId = 12345L;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramBotRepository telegramBotRepository;
    @Mock
    private UnknownTelegramUserRepository unknownTelegramUserRepository;
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
    public void onUpdateReceivedWithStartCommandAndUnknownUserThatWasRegisteredBeforeTest() throws Exception {
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND);
        TelegramBot telegramBot = new TelegramBot();

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.of(telegramBot));
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndUnknownUserThatAlreadyUsingBotTest() throws Exception {
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND);
        UnknownTelegramUser unknownTelegramUser = new UnknownTelegramUser();

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(unknownTelegramUserRepository.findById(tgUserId)).thenReturn(Optional.of(unknownTelegramUser));
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(unknownTelegramUserRepository, times(1)).findById(tgUserId);
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndUnknownUserFirstTimeJoinBotTest() throws Exception {
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND);

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(unknownTelegramUserRepository, times(1)).save(any(UnknownTelegramUser.class));
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndUnknownUserWhoWasRegisteredBeforeTest() {
        String uuId = UUID.randomUUID().toString();
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND + " " + uuId);
        User user = ModelUtils.getUser();
        UnknownTelegramUser unknownSavedTelegramUser = new UnknownTelegramUser();

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(user));
        when(unknownTelegramUserRepository.findById(tgUserId)).thenReturn(Optional.of(unknownSavedTelegramUser));
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(unknownTelegramUserRepository, times(1)).delete(unknownSavedTelegramUser);
        verify(telegramBotRepository, times(1)).save(any(TelegramBot.class));
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndAuthorizedUserStartedBotTest() throws Exception {
        String uuId = UUID.randomUUID().toString();
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND + " " + uuId);
        User user = ModelUtils.getUserWithBotNotifyFalse();

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.empty());
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(userRepository, times(1)).findUserByUuid(uuId);
        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
        verify(telegramBotRepository, times(1)).save(any(TelegramBot.class));
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndAuthorizedUserThatAlreadyJoinedTest() throws Exception {
        String uuId = UUID.randomUUID().toString();
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND + " " + uuId);
        User user = ModelUtils.getUserWithBotNotifyFalse();
        TelegramBot telegramBot = new TelegramBot();

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(user));
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.of(telegramBot));
        doNothing().when(executor).executeCommand(any(TelegramLongPollingBot.class), any(SendMessage.class));

        ubsTelegramBot.onUpdateReceived(update);

        verify(userRepository, times(1)).findUserByUuid(uuId);
        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
    }

    @Test
    public void onUpdateReceivedWithStartCommandAndWithInvalidUserUuIdTest() throws Exception {
        String uuId = UUID.randomUUID().toString();
        Update update = createUpdate(tgUserId, AppConstant.TELEGRAM_START_COMMAND + " " + uuId);

        when(update.getMessage().getFrom().getId()).thenReturn(tgUserId);
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsTelegramBot.onUpdateReceived(update));

        verify(userRepository, times(1)).findUserByUuid(uuId);
    }

    private Update createUpdate(Long tgUserId, String messageText) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(tgUserId);
        org.telegram.telegrambots.meta.api.objects.User from =
            mock(org.telegram.telegrambots.meta.api.objects.User.class);
        message.setFrom(from);
        message.setText(messageText);
        message.setChat(chat);
        update.setMessage(message);

        return update;
    }
}
