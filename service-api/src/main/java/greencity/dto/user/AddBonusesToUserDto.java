package greencity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
