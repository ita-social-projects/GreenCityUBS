package greencity.properties;

import greencity.constant.ErrorMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сlass for retrieving configuration values from the runtime environment. Used
 * to access dynamic properties. Provides a flexible alternative to @Value,
 * always getting the latest values without having to restart the application or
 * use /actuator/refresh.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramProperties {
    private final Environment environment;

    @PostConstruct
    public void validateProperties() {
        getTelegramBotName();
        getTelegramBotToken();
        getUbsAdminBaseUrl();
        log.info("All Telegram properties validated successfully.");
    }

    public String getTelegramBotName() {
        String telegramBotName = environment.getProperty("greencity.bots.ubs-bot-name");
        if (!StringUtils.hasText(telegramBotName)) {
            log.error(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND);
        }
        return telegramBotName;
    }

    public String getTelegramBotToken() {
        String telegramBotToken = environment.getProperty("greencity.bots.ubs-bot-token");
        if (!StringUtils.hasText(telegramBotToken)) {
            log.error(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND);
        }
        return telegramBotToken;
    }

    public String getUbsAdminBaseUrl() {
        String ubsAdminBaseUrl = environment.getProperty("greencity.bots.ubs-bot-ui");
        if (!StringUtils.hasText(ubsAdminBaseUrl)) {
            log.error(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND);
        }
        return ubsAdminBaseUrl;
    }
}
