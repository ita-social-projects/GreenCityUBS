package greencity.config;

import greencity.properties.TelegramProperties;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.UBSTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Profile("!test")
@RequiredArgsConstructor
@Configuration
public class TelegramBotConfig {
    private final TelegramProperties telegramProperties;

    @Bean
    public UBSTelegramBot ubsTelegramBot(@Lazy TelegramService telegramService) {
        return new UBSTelegramBot(telegramProperties.getTelegramBotToken(), telegramProperties.getTelegramBotName(), telegramService);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(UBSTelegramBot ubsTelegramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(ubsTelegramBot);
        return telegramBotsApi;
    }
}
