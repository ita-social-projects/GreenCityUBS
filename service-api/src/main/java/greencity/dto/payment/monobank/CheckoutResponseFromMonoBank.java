package greencity.dto.payment.monobank;

import lombok.Builder;

@Builder
public record CheckoutResponseFromMonoBank(
    String invoiceId,
    String pageUrl) {
}
