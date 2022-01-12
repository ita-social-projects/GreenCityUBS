package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentInfoDto {
    Long id;
    String settlementdate;
    Long paymentId;
    Long amount;
    String comment;
    String imagePath;
}
