package greencity.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сlass for retrieving configuration values from the runtime environment.
 * Used to access dynamic properties.
 * Provides a flexible alternative to @Value, always getting the latest values
 * without having to restart the application or use /actuator/refresh.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramProperties {
    private final Environment environment;

    public String getTelegramBotName() {
        String telegramBotName = environment.getProperty("greencity.bots.ubs-bot-name");
        if (!StringUtils.hasText(telegramBotName)) {
            log.error("The greencity bots name is empty");
        }
        return telegramBotName;
    }

    public String getTelegramBotToken() {
        String telegramBotToken = environment.getProperty("greencity.bots.ubs-bot-token");
        if (!StringUtils.hasText(telegramBotToken)) {
            log.error("The greencity bots token is empty");
        }
        return telegramBotToken;
    }

    public String getUbsAdminBaseUrl(){
        String ubsAdminBaseUrl = environment.getProperty("greencity.bots.ubs-bot-ui");
        if (!StringUtils.hasText(ubsAdminBaseUrl)) {
            log.error("The greencity admin base url is empty");
        }
        return ubsAdminBaseUrl;
    }
}
