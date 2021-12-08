package greencity.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class LimitsDto {
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
}
