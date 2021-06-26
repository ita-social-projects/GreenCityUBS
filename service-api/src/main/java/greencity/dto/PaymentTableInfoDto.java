package greencity.dto;

import java.util.List;
import lombok.*;

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
}
