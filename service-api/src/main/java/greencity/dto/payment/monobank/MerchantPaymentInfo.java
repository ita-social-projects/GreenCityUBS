package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MerchantPaymentInfo {
    @JsonProperty("reference")
    private String orderReference;
    @JsonProperty("customerEmails")
    private Set<String> emails;
    @JsonProperty("basketOrder")
    private List<BasketOrder> orderList;
}
