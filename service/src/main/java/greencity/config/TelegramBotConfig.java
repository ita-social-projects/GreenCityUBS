package greencity.config;

import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;

    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;

    @Bean
    public UBSTelegramBot ubsTelegramBot(TelegramService telegramService) {
        return new UBSTelegramBot(botToken, botName, telegramService);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(UBSTelegramBot ubsTelegramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(ubsTelegramBot);
        return telegramBotsApi;
    }
}
