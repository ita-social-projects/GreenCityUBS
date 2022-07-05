package greencity.dto.payment;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PaymentTableInfoDto {
    Long paidAmount;
    Long unPaidAmount;
    List<PaymentInfoDto> paymentInfoDtos;
    Long overpayment;
}
