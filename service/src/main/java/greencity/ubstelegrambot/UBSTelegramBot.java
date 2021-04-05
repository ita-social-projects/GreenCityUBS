package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import greencity.exceptions.TelegramBotAlreadyConnected;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${ubs.bot.name}")
    private String botName;
    @Value("${ubs.bot.token}")
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
        User user = userRepository.findByUuid(uuId);
        if (user.getTelegramBot() == null) {
            telegramBotRepository.save(TelegramBot.builder()
                .chatId(message.getChatId())
                .user(user)
                .build());
            TelegramBot telegramBot = telegramBotRepository.findByChatId(message.getChatId());
            user.setTelegramBot(telegramBot);
            userRepository.save(user);
        } else {
            throw new TelegramBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT);
        }
    }
}
