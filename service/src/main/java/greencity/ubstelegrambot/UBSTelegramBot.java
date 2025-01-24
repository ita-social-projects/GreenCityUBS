package greencity.ubstelegrambot;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.entity.telegram.UnknownTelegramUser;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.bots.TelegramBotAlreadyConnected;
import greencity.repository.UnknownTelegramUserRepository;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;
    private final UserRepository userRepository;
    private final TelegramBotRepository telegramBotRepository;
    private final UnknownTelegramUserRepository unknownTelegramUserRepository;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.getText().startsWith(AppConstant.TELEGRAM_START_COMMAND)) {
            String uuId = message.getText().replace(AppConstant.TELEGRAM_START_COMMAND, "").trim();
            final Long tgUserId = message.getFrom().getId();

            if (uuId.isEmpty()) {
                Optional<TelegramBot> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
                if (registeredUserBot.isPresent()) {
                    welcomeUser(tgUserId.toString());
                    return;
                }

                Optional<UnknownTelegramUser> unknownSavedUserBot = unknownTelegramUserRepository.findById(tgUserId);
                if (unknownSavedUserBot.isPresent()) {
                    welcomeUser(tgUserId.toString());
                    return;
                }

                UnknownTelegramUser unknownTelegramUser = UnknownTelegramUser.builder()
                        .id(update.getMessage().getFrom().getId())
                        .firstName(update.getMessage().getFrom().getFirstName())
                        .lastName(update.getMessage().getFrom().getLastName())
                        .userName(update.getMessage().getFrom().getUserName())
                        .build();
                unknownTelegramUserRepository.save(unknownTelegramUser);
                welcomeUser(tgUserId.toString());
                return;
            }

            User user = userRepository.findUserByUuid(uuId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
            Optional<UnknownTelegramUser> unknownSavedTelegramUser = unknownTelegramUserRepository.findById(tgUserId);
            if (unknownSavedTelegramUser.isPresent()) {
                unknownTelegramUserRepository.delete(unknownSavedTelegramUser.get());
                telegramBotRepository.save(getTelegramBot(user, tgUserId));
                welcomeUser(tgUserId.toString());
                return;
            }

            Optional<TelegramBot> registeredUserBot = telegramBotRepository.findByChatId(tgUserId);
            if (registeredUserBot.isEmpty()) {
                telegramBotRepository.save(getTelegramBot(user, tgUserId));
                welcomeUser(tgUserId.toString());
                return;
            } else {
                throw new TelegramBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT);
            }
        }
    }

    private TelegramBot getTelegramBot(User user, Long chatId) {
        TelegramBot telegramBot = user.getTelegramBot();
        if (telegramBot == null) {
            telegramBot = TelegramBot.builder()
                    .chatId(chatId)
                    .user(user)
                    .isNotify(true)
                    .build();
        } else if (!telegramBot.getIsNotify().booleanValue()) {
            telegramBot.setIsNotify(true);
        }
        return telegramBotRepository.save(telegramBot);
    }

    private void welcomeUser(String chatId) {
        SendMessage sendMessage =
                new SendMessage(chatId, AppConstant.TELEGRAM_GREETING_MESSAGE);
        try {
            execute(sendMessage);
        } catch (Exception e) {
            throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
        }
    }
}
