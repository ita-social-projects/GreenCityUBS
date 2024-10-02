package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record BasketOrder(
    String name,
    @JsonProperty("qty") Float quantity,
    Integer sum,
    String code) {
}
