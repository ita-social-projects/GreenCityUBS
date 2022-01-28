package greencity.config;

import com.liqpay.LiqPay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiqPayConfiguration {
    @Value("${greencity.payment.liq-pay-public-key}")
    private String publicKey;
    @Value("${greencity.payment.liq-pay-private-key}")
    private String privateKey;

    /**
     * Create LiqPay object that accept private and public key.
     * 
     * @return {@link LiqPay}
     */
    @Bean
    public LiqPay liqPay() {
        return new LiqPay(publicKey, privateKey);
    }
}
