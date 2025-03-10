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
            .districtUk(dtoUpdate.getAddress().getDistrictUk())
            .districtEn(dtoUpdate.getAddress().getDistrictEn())
            .streetUk(dtoUpdate.getAddress().getStreetUk())
            .streetEn(dtoUpdate.getAddress().getStreetEn())
            .houseCorpus(dtoUpdate.getAddress().getHouseCorpus())
            .entranceNumber(dtoUpdate.getAddress().getEntranceNumber())
            .houseNumber(dtoUpdate.getAddress().getHouseNumber())
            .build();
    }
}