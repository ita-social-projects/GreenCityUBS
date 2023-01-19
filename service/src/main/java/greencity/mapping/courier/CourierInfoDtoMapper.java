package greencity.mapping.courier;

import greencity.dto.courier.CourierInfoDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;

public class CourierInfoDtoMapper extends AbstractConverter<TariffsInfo, CourierInfoDto> {
    @Override
    protected CourierInfoDto convert(TariffsInfo courierLocation) {
        return CourierInfoDto.builder()
            .courierLimit(courierLocation.getCourierLimit())
            .maxQuantity(courierLocation.getMaxQuantity())
            .minQuantity(courierLocation.getMinQuantity())
            .build();
    }
}
