package greencity.mapping;

import greencity.dto.BagInfoDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;

public class CapacityAndPriceMapper extends AbstractConverter<Bag, BagInfoDto> {
    @Override
    protected BagInfoDto convert(Bag bag) {
        return BagInfoDto.builder()
            .capacity(bag.getCapacity())
            .price(bag.getPrice())
            .id(bag.getId())
            .build();
    }
}
