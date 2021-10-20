package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourierDto {
    @NotNull
    private Long minAmountOfBigBags;
    @NotNull
    private Long maxAmountOfBigBags;
    @NotNull
    private Long minPriceOfOrder;
    @NotNull
    private Long maxPriceOfOrder;
    @NotNull
    private Long locationId;
    @NotNull
    List<CreateCourierTranslationDto> createCourierTranslationDtos;
}
