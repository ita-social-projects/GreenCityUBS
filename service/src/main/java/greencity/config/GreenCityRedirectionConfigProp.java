package greencity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "redirect", ignoreUnknownFields = false)
public class GreenCityRedirectionConfigProp {
    private String confirmPage;
}
