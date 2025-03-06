package greencity.mapping.bag;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.bag.BagInfoDto;
import greencity.entity.order.OrderBag;
import greencity.exceptions.BadRequestException;
import java.math.BigDecimal;
import java.util.Objects;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderBagToBagInfoDtoMapper extends AbstractConverter<OrderBag, BagInfoDto> {
    @Override
    protected BagInfoDto convert(OrderBag source) {
        validateSource(source);

        return BagInfoDto.builder()
            .id(source.getBag().getId())
            .price(BigDecimal.valueOf(source.getPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .capacity(source.getCapacity())
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
            .build();
    }

    private void validateSource(OrderBag source) {
        if (Objects.isNull(source) || Objects.isNull(source.getBag())) {
            throw new BadRequestException(ErrorMessage.MAPPER_ERROR);
        }
    }
}
