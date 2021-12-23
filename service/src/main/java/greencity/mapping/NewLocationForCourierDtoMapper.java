package greencity.mapping;

import greencity.dto.NewLocationForCourierDto;
import greencity.entity.enums.CourierLimit;
import greencity.entity.order.CourierLocation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class NewLocationForCourierDtoMapper extends AbstractConverter<NewLocationForCourierDto, CourierLocation> {
    @Override
    protected CourierLocation convert(NewLocationForCourierDto source) {
        return CourierLocation.builder()
            .minPriceOfOrder(source.getMinAmountOfOrder())
            .maxPriceOfOrder(source.getMaxAmountOfOrder())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBag())
            .minAmountOfBigBags(source.getMinAmountOfBigBag())
            .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
            .build();
    }
}
