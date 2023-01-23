package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long minQuantity;
    @NotNull
    @Min(0)
    private Long maxQuantity;
    private Long locationId;
}
