package greencity.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "greencity.payment", ignoreUnknownFields = false)
public class PaymentConfigProp {
    private String merchantId;

    private String fondyPaymentKey;

}
