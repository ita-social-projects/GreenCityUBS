package greencity.mapping.bag;

import greencity.dto.bag.BagTransDto;
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
