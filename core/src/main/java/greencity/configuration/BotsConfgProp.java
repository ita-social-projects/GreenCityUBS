package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity.bots", ignoreUnknownFields = false)
public class BotsConfgProp {
    private String ubsBotName;
    private String ubsBotToken;
    private String viberBotUri;
    private String viberBotUrl;
    private String viberBotToken;
}
