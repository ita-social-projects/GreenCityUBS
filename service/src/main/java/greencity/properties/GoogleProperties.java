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
public class GoogleProperties {
    private final Environment environment;

    public String getGoogleApiKey() {
        String googleApiKey = environment.getProperty("greencity.authorization.googleApiKey");
        if (!StringUtils.hasText(googleApiKey)) {
            log.error("Google API Key not set");
        }
        return googleApiKey;
    }
}
