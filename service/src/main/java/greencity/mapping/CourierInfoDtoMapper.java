package greencity.mapping;

import greencity.dto.CourierInfoDto;
import greencity.entity.order.CourierLocation;
import org.modelmapper.AbstractConverter;

public class CourierInfoDtoMapper extends AbstractConverter<CourierLocation, CourierInfoDto> {
    @Override
    protected CourierInfoDto convert(CourierLocation courierLocation) {
        return CourierInfoDto.builder()
            .courierLimit(courierLocation.getCourierLimit())
            .maxAmountOfBigBags(courierLocation.getMaxAmountOfBigBags())
            .maxPriceOfOrder(courierLocation.getMaxPriceOfOrder())
            .minAmountOfBigBags(courierLocation.getMinAmountOfBigBags())
            .minPriceOfOrder(courierLocation.getMaxPriceOfOrder())
            .build();
    }
}
