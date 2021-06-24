package greencity.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class UpdateOrderDetailDto {
    @Min(0)
    @Max(value = 999, message = "value must < 1000")
    @NotNull
    Integer amount;
    @Min(0)
    @Max(value = 999, message = "value must < 1000")
    @NotNull
    Integer exportedQuantity;
    @Min(0)
    @Max(999)
    @NotNull
    Integer confirmedQuantity;
    @NotNull
    Long orderId;
    @NotNull
    Integer bagId;
}
