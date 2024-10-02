package greencity.dto.order;

import lombok.Builder;

@Builder
public record PaymentSystemResponse(
    Long orderId,
    String link) {
}
