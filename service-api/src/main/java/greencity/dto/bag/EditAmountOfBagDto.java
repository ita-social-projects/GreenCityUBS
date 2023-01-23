package greencity.dto.bag;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditAmountOfBagDto {
    @NotNull
    @Min(1)
    private Long minQuantity;
    @NotNull
    @Min(1)
    private Long maxQuantity;
    private Long locationId;
}
