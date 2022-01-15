package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity", ignoreUnknownFields = false)
public class GreenCityConfigurationProperties {
    private String tokenKey;

    private String userServerAddress;

    private String ubsBotName;

    private String ubsBotToken;

    private String viberBotUrl;

    private String viberBotToken;

    private String merchantId;

    private String fondyPaymentKey;

    private String liqPayPublicKey;

    private String liqPayPrivateKey;
}