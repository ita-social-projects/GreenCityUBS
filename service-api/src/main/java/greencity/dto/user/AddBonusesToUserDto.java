package greencity.dto.user;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBonusesToUserDto {
    private Long amount;
    private String receiptLink;
    private String settlementdate;
    private String paymentId;
}
