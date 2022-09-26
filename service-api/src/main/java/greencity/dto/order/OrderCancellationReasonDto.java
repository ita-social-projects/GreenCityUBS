package greencity.dto.order;

import greencity.enums.CancellationReason;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderCancellationReasonDto {
    private CancellationReason cancellationReason;
    private String cancellationComment;
}
