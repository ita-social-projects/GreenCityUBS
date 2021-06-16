package greencity.mapping;

import greencity.dto.BagTransDto;
import greencity.entity.order.BagTranslation;
import org.modelmapper.AbstractConverter;

public class BagsMapper extends AbstractConverter<BagTranslation, BagTransDto> {
    @Override
    protected BagTransDto convert(BagTranslation bag) {
        return BagTransDto.builder()
            .name(bag.getName())
            .build();
    }
}
