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
public class GoogleProperties {
    private final Environment environment;

    @PostConstruct
    public void validateProperties() {
        getGoogleApiKey();
        log.info("All Google properties validated successfully.");
    }

    public String getGoogleApiKey() {
        String googleApiKey = environment.getProperty("greencity.authorization.googleApiKey");
        if (!StringUtils.hasText(googleApiKey)) {
            log.error(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND);
        }
        return googleApiKey;
    }
}
