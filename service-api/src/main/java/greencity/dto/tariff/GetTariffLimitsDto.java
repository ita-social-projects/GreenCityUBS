package greencity.dto.tariff;

import greencity.enums.CourierLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTariffLimitsDto {
    @NotNull
    private CourierLimit courierLimit;
    private Long min;
    private Long max;
}
