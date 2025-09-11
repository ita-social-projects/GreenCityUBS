package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UBSTelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final TelegramService telegramService;

    public UBSTelegramBot(String botToken, String botName, TelegramService telegramService) {
        super(botToken);
        this.botName = botName;
        this.telegramService = telegramService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramService.processUpdate(update);
    }
}
