package greencity.dto.tariff;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;

@Builder
@Data
public class SetTariffLimitsDto {
    @Min(0)
    private Long minAmountOfBigBags;
    @Min(0)
    private Long maxAmountOfBigBags;

    @Min(0)
    private Long minPriceOfOrder;
    @Min(0)
    private Long maxPriceOfOrder;
}
