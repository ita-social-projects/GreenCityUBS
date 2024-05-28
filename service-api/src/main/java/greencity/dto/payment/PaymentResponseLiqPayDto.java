package greencity.dto.payment;

import lombok.Data;

@Data
public class PaymentResponseLiqPayDto {
    private String data;
    private String signature;
}
