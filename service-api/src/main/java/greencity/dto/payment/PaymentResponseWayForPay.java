package greencity.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseWayForPay {
    private String orderReference;
    private String status;
    private String time;
    private String signature;
}
