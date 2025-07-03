package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;
    private TelegramService telegramService;

    public UBSTelegramBot(String botToken, String botName, TelegramService telegramService) {
        super(botToken);
        this.botToken = botToken;
        this.botName = botName;
        this.telegramService = telegramService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            telegramService.processTextCommand(update);
        } else if (update.hasCallbackQuery()) {
            telegramService.processCallBackQuery(update);
        }
    }
}
