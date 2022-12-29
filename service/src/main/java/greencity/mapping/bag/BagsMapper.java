package greencity.mapping.bag;

import greencity.dto.bag.BagTransDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;

public class BagsMapper extends AbstractConverter<Bag, BagTransDto> {
    @Override
    protected BagTransDto convert(Bag bag) {
        return BagTransDto.builder()
            .name(bag.getName())
            .build();
    }
}
