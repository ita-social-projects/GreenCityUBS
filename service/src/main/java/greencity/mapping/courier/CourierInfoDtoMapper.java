package greencity.mapping.courier;

import greencity.dto.courier.CourierInfoDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;

public class CourierInfoDtoMapper extends AbstractConverter<TariffsInfo, CourierInfoDto> {
    @Override
    protected CourierInfoDto convert(TariffsInfo courierLocation) {
        return CourierInfoDto.builder()
            .courierLimit(courierLocation.getCourierLimit())
            .max(courierLocation.getMax())
            .min(courierLocation.getMin())
            .build();
    }
}
