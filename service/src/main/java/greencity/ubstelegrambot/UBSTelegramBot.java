package greencity.ubstelegrambot;

import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramUpdateProcessor;
import greencity.ubstelegrambot.service.TelegramExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class UBSTelegramBot extends TelegramLongPollingBot {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;
    private TelegramService telegramService;
    private ApplicationContext applicationContext;
    private TelegramExecutor executor;

    public UBSTelegramBot(String botToken, String botName, TelegramService telegramService,
        ApplicationContext applicationContext, TelegramExecutor executor) {
        super(botToken);
        this.botName = botName;
        this.telegramService = telegramService;
        this.applicationContext = applicationContext;
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
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        executor.executeCommand(bot, sendMessage);

    }
}
