package greencity.ubstelegrambot;

import greencity.ModelUtils;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.bots.TelegramBotAlreadyConnected;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.Optional;
import java.util.UUID;
import static greencity.ModelUtils.getUserWithBotNotifyTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramBotRepository telegramBotRepository;
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
    void onUpdateReceivedThrowMessageWasNotSent1() {
        User user = ModelUtils.getUser();
        User userWithBot = getUserWithBotNotifyTrue();
        TelegramBot telegramBotTrue = ModelUtils.getTelegramBotNotifyTrue();
        TelegramBot telegramBotWithNullId = telegramBotTrue.setId(null);
        String uuid = UUID.randomUUID().toString();

        Update update = new Update();
        Message message = mock(Message.class);
        update.setMessage(message);

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(message.getChatId()).thenReturn(telegramBotTrue.getChatId());
        when(message.getText()).thenReturn("/start" + uuid);
        when(telegramBotRepository.findByUserAndChatIdAndIsNotify(user, message.getChatId(), true))
            .thenReturn(Optional.empty());
        when(telegramBotRepository.save(telegramBotWithNullId)).thenReturn(telegramBotTrue);
        when(userRepository.save(user)).thenReturn(userWithBot);

        assertThrows(MessageWasNotSent.class,
            () -> ubsTelegramBot.onUpdateReceived(update));

        verify(userRepository).findUserByUuid(uuid);
        verify(message, atLeast(1)).getChatId();
        verify(message, atLeast(1)).getText();
        verify(telegramBotRepository).findByUserAndChatIdAndIsNotify(user, message.getChatId(), true);
        verify(telegramBotRepository).save(telegramBotWithNullId);
        verify(userRepository).save(user);
    }

    @Test
    void onUpdateReceivedThrowMessageWasNotSent2() {
        User user = ModelUtils.getUserWithBotNotifyFalse();
        User userWithBot = getUserWithBotNotifyTrue();
        TelegramBot telegramBotTrue = ModelUtils.getTelegramBotNotifyTrue();
        String uuid = UUID.randomUUID().toString();

        Update update = new Update();
        Message message = mock(Message.class);
        update.setMessage(message);

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(message.getChatId()).thenReturn(telegramBotTrue.getChatId());
        when(message.getText()).thenReturn("/start" + uuid);
        when(telegramBotRepository.findByUserAndChatIdAndIsNotify(user, message.getChatId(), true))
            .thenReturn(Optional.empty());
        when(telegramBotRepository.save(telegramBotTrue)).thenReturn(telegramBotTrue);
        when(userRepository.save(user)).thenReturn(userWithBot);

        assertThrows(MessageWasNotSent.class,
            () -> ubsTelegramBot.onUpdateReceived(update));

        verify(userRepository).findUserByUuid(uuid);
        verify(message, atLeast(1)).getChatId();
        verify(message, atLeast(1)).getText();
        verify(telegramBotRepository).findByUserAndChatIdAndIsNotify(user, message.getChatId(), true);
        verify(telegramBotRepository).save(telegramBotTrue);
        verify(userRepository).save(user);
    }

    @Test
    void onUpdateReceivedThrowTelegramBotAlreadyConnected() {
        Long chatId = 1234567824356L;
        String uuid = UUID.randomUUID().toString();
        User user = ModelUtils.getUser();
        TelegramBot telegramBot = ModelUtils.getTelegramBotNotifyTrue();

        Update update = new Update();
        Message message = mock(Message.class);
        update.setMessage(message);

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn("/start" + uuid);
        when(telegramBotRepository.findByUserAndChatIdAndIsNotify(user, message.getChatId(), true))
            .thenReturn(Optional.of(telegramBot));

        assertThrows(TelegramBotAlreadyConnected.class,
            () -> ubsTelegramBot.onUpdateReceived(update));

        verify(userRepository).findUserByUuid(uuid);
        verify(message, atLeast(1)).getChatId();
        verify(message, atLeast(1)).getText();
        verify(telegramBotRepository).findByUserAndChatIdAndIsNotify(user, message.getChatId(), true);
        verify(telegramBotRepository, never()).save(any(TelegramBot.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void onUpdateReceivedThrowNotFoundException() {
        String uuid = UUID.randomUUID().toString();

        Update update = new Update();
        Message message = mock(Message.class);
        update.setMessage(message);

        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());
        when(message.getText()).thenReturn("/start" + uuid);

        assertThrows(NotFoundException.class,
            () -> ubsTelegramBot.onUpdateReceived(update));

        verify(userRepository).findUserByUuid(uuid);
        verify(message, atLeast(1)).getText();
        verify(telegramBotRepository, never()).save(any(TelegramBot.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
