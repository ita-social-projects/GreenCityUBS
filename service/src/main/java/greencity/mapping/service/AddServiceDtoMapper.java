package greencity.mapping.service;

import greencity.dto.service.AddServiceDto;
import greencity.dto.tariff.TariffTranslationDto;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddServiceDtoMapper extends AbstractConverter<Bag, AddServiceDto> {
    @Override
    protected AddServiceDto convert(Bag source) {
        List<BagTranslation> translations = source.getBagTranslations();
        List<TariffTranslationDto> tariffTranslationDtoList = translations.stream().map(
            bagTranslation -> new TariffTranslationDto(
                bagTranslation.getName(),
                bagTranslation.getNameEng(),
                bagTranslation.getDescription(),
                bagTranslation.getDescriptionEng()))
            .collect(Collectors.toList());
        return AddServiceDto.builder()
            .locationId(source.getLocation().getId())
            .commission(source.getCommission())
            .capacity(source.getCapacity())
            .price(source.getPrice())
            .tariffTranslationDtoList(tariffTranslationDtoList)
            .build();
    }
}
