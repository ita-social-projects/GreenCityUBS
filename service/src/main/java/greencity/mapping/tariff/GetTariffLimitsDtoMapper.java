package greencity.mapping.tariff;

import greencity.dto.tariff.GetTariffLimitsDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class GetTariffLimitsDtoMapper extends AbstractConverter<TariffsInfo, GetTariffLimitsDto> {
    @Override
    protected GetTariffLimitsDto convert(TariffsInfo source) {
        return GetTariffLimitsDto.builder()
            .min(source.getMin())
            .max(source.getMax())
            .courierLimit(source.getCourierLimit())
            .build();
    }
}
