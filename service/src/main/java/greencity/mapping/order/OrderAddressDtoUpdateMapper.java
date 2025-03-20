package greencity.mapping.order;

import greencity.dto.order.OrderAddressDtoResponse;
import greencity.entity.user.ubs.OrderAddress;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderAddressDtoUpdateMapper extends AbstractConverter<OrderAddress, OrderAddressDtoResponse> {
    @Override
    protected OrderAddressDtoResponse convert(OrderAddress dtoUpdate) {
        return OrderAddressDtoResponse.builder()
            .districtUk(dtoUpdate.getBaseAddress().getDistrictUk())
            .districtEn(dtoUpdate.getBaseAddress().getDistrictEn())
            .streetUk(dtoUpdate.getBaseAddress().getStreetUk())
            .streetEn(dtoUpdate.getBaseAddress().getStreetEn())
            .houseCorpus(dtoUpdate.getBaseAddress().getHouseCorpus())
            .entranceNumber(dtoUpdate.getBaseAddress().getEntranceNumber())
            .houseNumber(dtoUpdate.getBaseAddress().getHouseNumber())
            .build();
    }
}