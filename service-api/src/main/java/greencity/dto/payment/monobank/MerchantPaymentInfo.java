package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record MerchantPaymentInfo(
    @JsonProperty("reference") String orderReference,
    @JsonProperty("customerEmails") Set<String> emails,
    @JsonProperty("basketOrder") List<BasketOrder> orderList) {
}
