package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.service.TelegramExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    private TelegramService telegramService;
    private TelegramExecutor executor;

    public UBSTelegramBot(String botToken, String botName, TelegramService telegramService,
        TelegramExecutor executor) {
        super(botToken);
        this.botName = botName;
        this.telegramService = telegramService;
        this.executor = executor;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        TelegramUpdateProcessor updateProcessor = telegramService.processUpdate(update);
        SendMessage sendMessage = updateProcessor.process(update);
        if (sendMessage != null) {
            executor.executeCommand(this, sendMessage);
        }
    }
}
