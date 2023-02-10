package greencity.mapping.service;

import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class TariffServiceDtoMapper extends AbstractConverter<Bag, TariffServiceDto> {
    @Override
    protected TariffServiceDto convert(Bag source) {
        return TariffServiceDto.builder()
            .capacity(source.getCapacity())
            .price(source.getPrice())
            .commission(source.getCommission())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .build();
    }
}
