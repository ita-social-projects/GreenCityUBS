package greencity.mapping.bag;

import greencity.constant.AppConstant;
import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.OrderBag;
import java.math.BigDecimal;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderBagToBagInfoDtoMapper extends AbstractConverter<OrderBag, BagInfoDto> {
    @Override
    protected BagInfoDto convert(OrderBag source) {
        return BagInfoDto.builder()
            .id(source.getBag().getId())
            .price(BigDecimal.valueOf(source.getPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .capacity(source.getCapacity())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .build();
    }
}
