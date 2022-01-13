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
    String paymentId;
    Long amount;
    String comment;
    String receiptLink;
    String imagePath;
}
