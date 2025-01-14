package greencity.mapping.bag;

import greencity.constant.AppConstant;
import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class BagInfoDtoMapper extends AbstractConverter<Bag, BagInfoDto> {
    @Override
    protected BagInfoDto convert(Bag bag) {
        return BagInfoDto.builder()
            .id(bag.getId())
            .name(bag.getName())
            .nameEng(bag.getNameEng())
            .capacity(bag.getCapacity())
            .price(BigDecimal.valueOf(bag.getFullPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .build();
    }
}
