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
    private Long min;
    @NotNull
    @Min(1)
    private Long max;
    private Long locationId;
}
