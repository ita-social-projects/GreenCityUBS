package greencity.dto.tariff;

import greencity.dto.bag.BagLimitDto;
import greencity.enums.CourierLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetTariffLimitsDto {
    private Long min;
    private Long max;
    @NotNull
    private CourierLimit courierLimit;
    private String limitDescription;
    @NotEmpty
    private List<BagLimitDto> bagLimitDtoList;
}
