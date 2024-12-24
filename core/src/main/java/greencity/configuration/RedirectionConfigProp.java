package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity.redirect", ignoreUnknownFields = false)
public class RedirectionConfigProp {
    private String userServerAddress;
    private String greenCityClient;
    private String resultUrlFondy;
    private String resultUrlFondyPersonalCabinet;
}
