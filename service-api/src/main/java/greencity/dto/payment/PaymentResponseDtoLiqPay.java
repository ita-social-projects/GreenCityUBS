package greencity.dto.payment;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentResponseDtoLiqPay {
    @NotNull
    private String data;
    @NotNull
    private String signature;
}
