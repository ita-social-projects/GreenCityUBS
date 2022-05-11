package greencity.dto.order;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditPriceOfOrder {
    @NotNull
    @Min(0)
    private Long minPriceOfOrder;
    @NotNull
    @Min(0)
    private Long maxPriceOfOrder;
    private Long locationId;
}
