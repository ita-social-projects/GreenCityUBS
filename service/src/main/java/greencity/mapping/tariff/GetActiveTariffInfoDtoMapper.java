package greencity.mapping.tariff;

import greencity.dto.tariff.GetActiveTariffInfoDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class GetActiveTariffInfoDtoMapper extends AbstractConverter<TariffsInfo, GetActiveTariffInfoDto> {
    @Override
    protected GetActiveTariffInfoDto convert(TariffsInfo source) {
        return GetActiveTariffInfoDto.builder()
            .id(source.getId())
            .tariffNameEn(source.getTariffNameEn())
            .tariffNameUk(source.getTariffNameUk())
            .build();
    }
}
