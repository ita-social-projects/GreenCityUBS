package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditPriceOfOrder {
    @NotNull
    private Long minPriceOfOrder;
    @NotNull
    private Long maxPriceOfOrder;
    @NotNull
    private Long locationId;
}
