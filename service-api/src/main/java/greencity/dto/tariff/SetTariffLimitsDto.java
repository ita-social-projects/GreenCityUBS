package greencity.dto.tariff;

import greencity.enums.CourierLimit;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Builder
@Data
public class SetTariffLimitsDto {
    @Min(0)
    private Long min;

    @Min(0)
    private Long max;

    @NotNull
    private CourierLimit courierLimit;
}
