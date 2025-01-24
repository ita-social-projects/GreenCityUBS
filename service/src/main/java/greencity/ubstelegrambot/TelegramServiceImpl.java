package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TelegramServiceImpl implements TelegramService {
    private final UnknownTelegramUserRepository unknownTelegramUserRepository;
    private final TelegramBotRepository telegramBotRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUnknownTelegramUser(Update update) {
        final Long tgUserId = update.getMessage().getFrom().getId();
        Optional<TelegramBot> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
        UnknownTelegramUser unknownTelegramUser = populateUnknownTelegramUser(update);

        if (registeredUserBot.isEmpty()) {
            unknownTelegramUserRepository.save(unknownTelegramUser);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAuthorizedUser(String uuId, Long tgUserId) {
        User user = userRepository.findUserByUuid(uuId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));

        Optional<UnknownTelegramUser> unknownSavedTelegramUser = unknownTelegramUserRepository.findById(tgUserId);
        if (unknownSavedTelegramUser.isPresent()) {
            unknownTelegramUserRepository.delete(unknownSavedTelegramUser.get());
            telegramBotRepository.save(createTelegramBotEntity(user, tgUserId));
            return;
        }

        Optional<TelegramBot> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
        if (registeredUserBot.isEmpty()) {
            telegramBotRepository.save(createTelegramBotEntity(user, tgUserId));
        }
    }

    private TelegramBot createTelegramBotEntity(User user, Long chatId) {
        return TelegramBot.builder()
            .chatId(chatId)
            .isNotify(true)
            .user(user)
            .build();
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
