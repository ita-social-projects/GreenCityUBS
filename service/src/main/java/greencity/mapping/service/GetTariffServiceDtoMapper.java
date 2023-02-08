package greencity.mapping.service;

import greencity.dto.service.GetTariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class GetTariffServiceDtoMapper extends AbstractConverter<Bag, GetTariffServiceDto> {
    @Override
    protected GetTariffServiceDto convert(Bag source) {
        return GetTariffServiceDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(source.getPrice())
            .fullPrice(source.getFullPrice())
            .commission(source.getCommission())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .limitIncluded(source.getLimitIncluded())
            .build();
    }
}
