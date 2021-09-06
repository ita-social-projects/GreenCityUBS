package greencity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentTableInfoDto {
    Long paidAmount;
    Long unPaidAmount;
    List<PaymentInfoDto> paymentInfoDtos;
    Long overpayment;
}
