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
public class AuthorizationProperties {
    private final Environment environment;

    @PostConstruct
    public void validateProperties() {
        getAccessTokenKey();
        getSystemEmailAddress();
        getSignInToken();
        log.info("All authorization properties validated successfully.");
    }

    public String getAccessTokenKey() {
        String accessTokenKey = environment.getProperty("greencity.authorization.token-key");
        if (!StringUtils.hasText(accessTokenKey)) {
            log.error(ErrorMessage.JWT_SECRET_KEY_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.JWT_SECRET_KEY_NOT_FOUND);
        }
        return accessTokenKey;
    }

    public String getSystemEmailAddress() {
        String systemEmailAddress = environment.getProperty("greencity.authorization.service-email");
        if (!StringUtils.hasText(systemEmailAddress)) {
            log.error(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND);
        }
        return systemEmailAddress;
    }

    public String getSignInToken() {
        String signIntoken = environment.getProperty("greencity.sing-in.secret-token");
        if (!StringUtils.hasText(signIntoken)) {
            log.error(ErrorMessage.SIGN_IN_TOKEN_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.SIGN_IN_TOKEN_NOT_FOUND);
        }
        return signIntoken;
    }
}
