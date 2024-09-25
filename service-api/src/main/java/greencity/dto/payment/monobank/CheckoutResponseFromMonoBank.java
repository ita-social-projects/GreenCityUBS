package greencity.dto.payment.monobank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CheckoutResponseFromMonoBank {
    private String invoiceId;
    private String pageUrl;
}
