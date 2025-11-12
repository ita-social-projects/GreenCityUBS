package greencity.properties;

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
public class AuthorizationProperties {
    private final Environment environment;

    public String getAccessTokenKey() {
        String accessTokenKey = environment.getProperty("greencity.authorization.token-key");
        if (!StringUtils.hasText(accessTokenKey)) {
            log.error("Authorization token key not set");
        }
        return accessTokenKey;
    }

    public String getSystemEmailAddress() {
        String systemEmailAddress = environment.getProperty("greencity.authorization.service-email");
        if (!StringUtils.hasText(systemEmailAddress)) {
            log.error("SystemEmailAddress property is empty");
        }
        return systemEmailAddress;
    }

    public String getSignInToken() {
        String signIntoken = environment.getProperty("greencity.sing-in.secret-token");
        if (!StringUtils.hasText(signIntoken)) {
            log.error("SingInToken property is empty");
        }
        return signIntoken;
    }
}
