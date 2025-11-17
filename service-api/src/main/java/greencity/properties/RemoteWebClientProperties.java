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
public class RemoteWebClientProperties {
    private final Environment environment;

    @PostConstruct
    public void validateProperties() {
        getGreenCityUserAddress();
        getWebClientConnectTimeout();
        getWebClientResponseTimeout();
        log.info("All Remote Web Client properties validated successfully.");
    }

    public String getGreenCityUserAddress() {
        String address = environment.getProperty("greencity.redirect.user-server-address");
        if (!StringUtils.hasText(address)) {
            log.error(ErrorMessage.USER_SERVER_ADDRESS_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.USER_SERVER_ADDRESS_NOT_FOUND);
        }
        return address;
    }

    public Integer getWebClientConnectTimeout() {
        Integer connectionTimeout = environment.getProperty("webclient.connection-timeout-millis", Integer.class);
        if (connectionTimeout == null) {
            log.error(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND);
        }
        return connectionTimeout;
    }

    public Integer getWebClientResponseTimeout() {
        Integer responseTimeout = environment.getProperty("webclient.response-timeout-millis", Integer.class);
        if (responseTimeout == null) {
            log.error(ErrorMessage.WEB_CLIENT_RESPONSE_TIMEOUT_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WEB_CLIENT_RESPONSE_TIMEOUT_NOT_FOUND);
        }
        return responseTimeout;
    }
}
