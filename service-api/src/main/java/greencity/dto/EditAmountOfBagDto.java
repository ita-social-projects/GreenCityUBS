package greencity.dto;

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
    private Long minAmountOfBigBags;
    @NotNull
    @Min(1)
    private Long maxAmountOfBigBags;
    private Long locationId;
}
