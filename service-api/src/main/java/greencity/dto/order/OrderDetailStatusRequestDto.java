package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailStatusRequestDto {
    String orderStatus;
    String orderPaymentStatus;
    String adminComment;
    String cancellationComment;
    String cancellationReason;
}