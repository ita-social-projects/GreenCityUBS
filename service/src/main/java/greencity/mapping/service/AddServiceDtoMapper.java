package greencity.mapping.service;

import greencity.dto.service.AddServiceDto;
import greencity.dto.tariff.TariffTranslationDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddServiceDtoMapper extends AbstractConverter<Bag, AddServiceDto> {
    @Override
    protected AddServiceDto convert(Bag source) {
        TariffTranslationDto tariffTranslationDto =
            TariffTranslationDto.builder().name(source.getName()).nameEng(source.getNameEng())
                .description(source.getDescription()).descriptionEng(source.getDescriptionEng()).build();

        return AddServiceDto.builder()
            .locationId(source.getLocation().getId())
            .commission(source.getCommission())
            .capacity(source.getCapacity())
            .price(source.getPrice())
            .tariffTranslationDtoList(tariffTranslationDto)
            .build();
    }
}
