package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity.external", ignoreUnknownFields = false)
public class ExternalUrlConfigProp {
    private String fondyApiUrl;
    private String viberApiUrl;
}
