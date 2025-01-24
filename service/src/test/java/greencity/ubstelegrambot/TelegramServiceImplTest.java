package greencity.ubstelegrambot;

import greencity.ModelUtils;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramServiceImplTest {
    final Long tgUserId = 12345L;
    String uuId = UUID.randomUUID().toString();
    @Mock
    private UnknownTelegramUserRepository unknownTelegramUserRepository;
    @Mock
    private TelegramBotRepository telegramBotRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TelegramServiceImpl telegramService;

    @Test
    public void handleUnknownTelegramUserWithUnknownUserTest() {
        Update update = createUpdate(tgUserId);
        UnknownTelegramUser unknownTelegramUser = populateUnknownTelegramUser(update);
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.empty());

        telegramService.handleUnknownTelegramUser(update);

        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
        verify(unknownTelegramUserRepository, times(1)).save(unknownTelegramUser);
    }

    @Test
    public void handleUnknownTelegramUserWithUnknownReturnedUserTest() {
        Update update = createUpdate(tgUserId);
        TelegramBot telegramBot = new TelegramBot();
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.of(telegramBot));

        telegramService.handleUnknownTelegramUser(update);

        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
    }

    @Test
    public void handleAuthorizedUserWithUserAlreadyJoinedBotAsUnknownTelegramUserTest() {
        Update update = createUpdate(tgUserId);
        UnknownTelegramUser unknownTelegramUser = populateUnknownTelegramUser(update);
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(ModelUtils.getUser()));
        when(unknownTelegramUserRepository.findById(tgUserId)).thenReturn(Optional.of(unknownTelegramUser));

        telegramService.handleAuthorizedUser(uuId, tgUserId);

        verify(userRepository, times(1)).findUserByUuid(uuId);
        verify(unknownTelegramUserRepository, times(1)).findById(tgUserId);
        verify(unknownTelegramUserRepository, times(1)).delete(unknownTelegramUser);
        verify(telegramBotRepository, times(1)).save(any(TelegramBot.class));
    }

    @Test
    public void handleAuthorizedUserWithValidAuthorizedUserTest() {
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(ModelUtils.getUser()));
        when(unknownTelegramUserRepository.findById(tgUserId)).thenReturn(Optional.empty());
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.empty());

        telegramService.handleAuthorizedUser(uuId, tgUserId);

        verify(userRepository, times(1)).findUserByUuid(uuId);
        verify(unknownTelegramUserRepository, times(1)).findById(tgUserId);
        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
        verify(telegramBotRepository, times(1)).save(any(TelegramBot.class));

    }

    @Test
    public void handleAuthorizedUserWithReturnedAuthorizedUserTest() {
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.of(ModelUtils.getUser()));
        when(unknownTelegramUserRepository.findById(tgUserId)).thenReturn(Optional.empty());
        when(telegramBotRepository.findByChatId(tgUserId)).thenReturn(Optional.of(new TelegramBot()));

        telegramService.handleAuthorizedUser(uuId, tgUserId);

        verify(userRepository, times(1)).findUserByUuid(uuId);
        verify(unknownTelegramUserRepository, times(1)).findById(tgUserId);
        verify(telegramBotRepository, times(1)).findByChatId(tgUserId);
    }

    @Test
    public void handleAuthorizedUserWithNotExistsUuIdTest() {
        when(userRepository.findUserByUuid(uuId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> telegramService.handleAuthorizedUser(uuId, tgUserId));

        verify(userRepository, times(1)).findUserByUuid(uuId);
    }

    private Update createUpdate(Long tgUserId) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(tgUserId);
        User from = new User();
        from.setId(tgUserId);
        from.setFirstName("Firstname");
        from.setLastName("Lastname");
        from.setUserName("Username");
        message.setFrom(from);
        message.setChat(chat);
        update.setMessage(message);

        return update;
    }

    private UnknownTelegramUser populateUnknownTelegramUser(Update update) {
        return UnknownTelegramUser.builder()
            .id(update.getMessage().getFrom().getId())
            .firstName(update.getMessage().getFrom().getFirstName())
            .lastName(update.getMessage().getFrom().getLastName())
            .userName(update.getMessage().getFrom().getUserName())
            .build();
    }
}
