package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.bots.TelegramBotAlreadyConnected;
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
        String uuId = message.getText().replace("/start", "").trim();
        User user = userRepository.findUserByUuid(uuId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        Optional<TelegramBot> telegramBotOptional =
            telegramBotRepository.findByUserAndChatIdAndIsNotify(user, message.getChatId(), true);
        if (telegramBotOptional.isEmpty() && message.getText().startsWith("/start")) {
            TelegramBot telegramBot = getTelegramBot(user, message.getChatId());
            user.setTelegramBot(telegramBot);
            userRepository.save(user);
            SendMessage sendMessage =
                new SendMessage(message.getChatId().toString(), "Вітаємо!\nВи підписались на UbsBot");
            try {
                execute(sendMessage);
            } catch (Exception e) {
                throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
            }
        } else {
            throw new TelegramBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT);
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
        } else if (!telegramBot.getIsNotify()) {
            telegramBot.setIsNotify(true);
        }
        return telegramBotRepository.save(telegramBot);
    }
}
