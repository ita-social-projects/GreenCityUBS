package greencity.mapping.bag;

import greencity.dto.bag.BagTranslationDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class BagTranslationDtoMapper extends AbstractConverter<Bag, BagTranslationDto> {
    @Override
    protected BagTranslationDto convert(Bag source) {
        return BagTranslationDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(source.getFullPrice())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .limitedIncluded(source.getLimitIncluded())
            .build();
    }
}
