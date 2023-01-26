package greencity.mapping.bag;

import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class BagInfoDtoMapper extends AbstractConverter<Bag, BagInfoDto> {
    @Override
    protected BagInfoDto convert(Bag bag) {
        return BagInfoDto.builder()
            .id(bag.getId())
            .name(bag.getName())
            .nameEng(bag.getNameEng())
            .capacity(bag.getCapacity())
            .price(bag.getFullPrice())
            .build();
    }
}
