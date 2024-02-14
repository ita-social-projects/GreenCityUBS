package greencity.dto.order;

import greencity.enums.CancellationReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
