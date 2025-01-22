package greencity.config;

import greencity.repository.TelegramBotRepository;
import greencity.repository.UserRepository;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;

    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;

    @Bean
    public UBSTelegramBot ubsTelegramBot(UserRepository userRepository,
        TelegramBotRepository telegramBotRepository, TelegramService telegramService) {
        return new UBSTelegramBot(botToken, botName, botToken,
            userRepository, telegramBotRepository, telegramService);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(UBSTelegramBot ubsTelegramBot) throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(ubsTelegramBot);
        return telegramBotsApi;
    }
}
