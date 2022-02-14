package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBonusesToUserDto {
    private Long amount;
    private String receiptLink;
    private String settlementdate;
    private String paymentId;
}
