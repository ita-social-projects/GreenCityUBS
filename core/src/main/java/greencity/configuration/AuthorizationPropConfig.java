package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity.authorization", ignoreUnknownFields = false)
public class AuthorizationPropConfig {
    private String tokenKey;
    private String googleApiKey;
}
