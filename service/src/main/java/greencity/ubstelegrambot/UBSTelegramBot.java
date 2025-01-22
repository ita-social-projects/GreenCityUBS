package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.bots.TelegramBotAlreadyConnected;
import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramNotificationService;
import greencity.ubstelegrambot.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;
    private final TelegramService telegramService;

    public UBSTelegramBot(String botToken, String botName, String token,
        TelegramService telegramService) {
        super(botToken);
        this.botName = botName;
        this.botToken = token;
        this.telegramService = telegramService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().getText().startsWith("/start")) {
            var message = update.getMessage();
            try {
                telegramService.initializeUserWithTelegramBot(message);
                execute(MessageFactory.creatWelcomeMessage(message.getChatId().toString()));
            } catch (TelegramApiException e) {
                throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
            }
        } else if (update.hasMessage() && update.getMessage().getText().startsWith("/help")) {
            var message = update.getMessage();
            try {
                execute(MessageFactory.createHelpMessage(message.getChatId().toString()));
            } catch (TelegramApiException e) {
                throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
            }
        } else {
            throw new TelegramBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_TELEGRAM_BOT);
        }
    }
}
